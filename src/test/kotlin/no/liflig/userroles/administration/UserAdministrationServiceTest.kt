package no.liflig.userroles.administration

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import no.liflig.userroles.roles.UserRole
import no.liflig.userroles.testutils.TestServices
import no.liflig.userroles.testutils.createRole
import no.liflig.userroles.testutils.createUserRole
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersRequest
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersResponse

class UserAdministrationServiceTest {
  @RegisterExtension private val services = TestServices.get()
  private val userAdministrationService = services.app.userAdministrationService
  private val userRoleRepo = services.app.userRoleRepo

  /** Utility function with defaults for tests. */
  private fun listUsers(
      limit: Int = DEFAULT_LIMIT,
      filter: UserFilter = createUserFilter(),
      cursor: UserCursor? = null,
  ): UserList {
    return userAdministrationService.listUsers(limit, filter, cursor)
  }

  @Test
  fun `test fetching single page`() {
    val userRoles =
        (1..3).map { number ->
          createUserRole(
              username = number.toString(),
              // Set different roles names, so we can verify that we join with correct roles
              createRole(roleName = "role-${number}"),
          )
        }

    services.mockCognito(
        object : MockCognitoClient {
          override fun listUsers(request: ListUsersRequest): ListUsersResponse {
            request.verify()

            return ListUsersResponse.builder()
                .users(userRoles.map { createCognitoUser(username = it.userId) })
                .build()
          }
        },
    )
    userRoleRepo.batchCreate(userRoles)

    val result = listUsers()
    result.nextCursor.shouldBeNull()
    result.users.shouldHaveSize(userRoles.size).forEachIndexed { index, user ->
      val userRole = userRoles[index]

      user.username.shouldBe(userRole.userId)
      user.roles.shouldBe(userRole.roles)
    }
  }

  @Test
  fun `test fetching multiple pages`() {
    val limit = 3
    val usernames = (0..7).map { it.toString() }

    val cognitoClient =
        object : MockCognitoClient {
          /** To verify the number of requests made to Cognito. */
          var requestCount = 0

          override fun listUsers(request: ListUsersRequest): ListUsersResponse {
            request.verify(
                expectedLimit = limit,
                // We verify pagination token below
                expectedPaginationToken = request.paginationToken(),
            )

            val (usernames: List<String>, paginationToken: String?) =
                when (request.paginationToken()) {
                  null -> Pair(usernames.slice(0..2), "token-1")
                  "token-1" -> Pair(usernames.slice(3..5), "token-2")
                  "token-2" -> Pair(usernames.slice(6..7), null)
                  else -> throw IllegalStateException()
                }

            this.requestCount++

            return ListUsersResponse.builder()
                .users(usernames.map { createCognitoUser(it) })
                .paginationToken(paginationToken)
                .build()
          }
        }
    services.mockCognito(cognitoClient)
    userRoleRepo.batchCreate(usernames.map { createUserRole(it) })

    val results = ArrayList<UserList>()
    do {
      results.add(listUsers(limit = limit, cursor = results.lastOrNull()?.nextCursor))
    } while (results.last().nextCursor != null)

    cognitoClient.requestCount.shouldBe(3)

    val (result1, result2, result3) = results.shouldHaveSize(3)

    result1.nextCursor.shouldBe(UserCursor("token-1", pageOffset = 0))
    result2.nextCursor.shouldBe(UserCursor("token-2", pageOffset = 0))
    result3.nextCursor.shouldBeNull()

    result1.users.map { it.username }.shouldBe(usernames.slice(0..2))
    result2.users.map { it.username }.shouldBe(usernames.slice(3..5))
    result3.users.map { it.username }.shouldBe(usernames.slice(6..7))
  }

  @Test
  fun `test search`() {
    services.mockCognito(
        object : MockCognitoClient {
          override fun listUsers(request: ListUsersRequest): ListUsersResponse {
            request.verify(expectedFilter = "username ^= \"test\"")
            return ListUsersResponse.builder().users(createCognitoUser()).build()
          }
        }
    )
    userRoleRepo.create(createUserRole())

    val result =
        userAdministrationService.listUsers(
            limit = DEFAULT_LIMIT,
            filter =
                createUserFilter(searchString = "test", searchField = UserSearchField.USERNAME),
            cursor = null,
        )
    result.users.shouldHaveSize(1)
  }

  @Test
  fun `test filtering by user role fields`() {
    /** Create user roles with all possible permutations of different app/org/role name. */
    val applications = listOf("app1", "app2")
    val orgs = listOf("org1", "org2")
    val roleNames = listOf("role1", "role2")

    var userCount = 0
    val userRoles: List<UserRole> =
        applications.flatMap { app ->
          orgs.flatMap { org ->
            roleNames.map { roleName ->
              userCount++

              createUserRole(
                  username = userCount.toString(),
                  createRole(applicationName = app, orgId = org, roleName = roleName),
              )
            }
          }
        }
    userRoles.shouldHaveSize(8)

    services.mockCognito(
        object : MockCognitoClient {
          override fun listUsers(request: ListUsersRequest): ListUsersResponse {
            request.verify()

            return ListUsersResponse.builder()
                .users(userRoles.map { createCognitoUser(it.userId) })
                .build()
          }
        }
    )
    userRoleRepo.batchCreate(userRoles)

    /** Test filtering on each field by itself. */
    val filteredByApp = listUsers(filter = createUserFilter(applicationName = "app1")).users
    filteredByApp
        .shouldHaveSize(4)
        .shouldEqualRoles(userRoles.filter { it.roles.first().applicationName == "app1" })

    val filteredByOrg = listUsers(filter = createUserFilter(orgId = "org1")).users
    filteredByOrg
        .shouldHaveSize(4)
        .shouldEqualRoles(userRoles.filter { it.roles.first().orgId == "org1" })

    val filteredByRole = listUsers(filter = createUserFilter(roleName = "role1")).users
    filteredByRole
        .shouldHaveSize(4)
        .shouldEqualRoles(userRoles.filter { it.roles.first().roleName == "role1" })

    /** Test filtering on 2 fields. */
    val filteredByAppAndOrg =
        listUsers(filter = createUserFilter(applicationName = "app2", orgId = "org2")).users
    filteredByAppAndOrg
        .shouldHaveSize(2)
        .shouldEqualRoles(
            userRoles.filter {
              val role = it.roles.first()
              role.applicationName == "app2" && role.orgId == "org2"
            }
        )

    /** Test filtering by all 3 fields. */
    val filteredByAppAndOrgAndRole =
        listUsers(
                filter =
                    createUserFilter(
                        applicationName = "app2",
                        orgId = "org1",
                        roleName = "role2",
                    )
            )
            .users
    filteredByAppAndOrgAndRole
        .shouldHaveSize(1)
        .shouldEqualRoles(
            userRoles.filter {
              val role = it.roles.first()
              role.applicationName == "app2" && role.orgId == "org1" && role.roleName == "role2"
            }
        )
  }

  @Test
  fun `test pagination with user role filters`() {
    val limit = 3
    /**
     * We want to test filtering by `orgId = "org1"`, so we create a list of user roles with some
     * that should match and some that should not.
     */
    val userRoles =
        listOf(
            /**
             * 1st page from Cognito: 2 matching roles, so [UserAdministrationService] should fetch
             * next page in order to fill `limit`.
             */
            createUserRole("1", createRole(orgId = "org1")),
            createUserRole("2", createRole(orgId = "org2")),
            createUserRole("3", createRole(orgId = "org1")),
            /**
             * 2nd page from Cognito: First user matches filter, so request 1 should stop after
             * that, since we have then reached our limit with 2 users from first page + 1 user from
             * this page.
             *
             * We expect this page to be fetched again on the subsequent request in order to get the
             * remaining users, but with `pageOffset = 1` to skip the user that was already added.
             */
            createUserRole("4", createRole(orgId = "org1")),
            createUserRole("5", createRole(orgId = "org1")),
            createUserRole("6", createRole(orgId = "org2")),
            /**
             * 3rd page from Cognito: The first 2 users here matches our filter, so they should fill
             * the limit along with the remaining user from page 2. The rest of the page does not
             * match our filter, so we expect this to return the pagination token of the next page,
             * and `pageOffset = 0`.
             */
            createUserRole("7", createRole(orgId = "org1")),
            createUserRole("8", createRole(orgId = "org1")),
            createUserRole("9", createRole(orgId = "org2")),
            /** 4th page from Cognito: All matches. */
            createUserRole("10", createRole(orgId = "org1")),
            createUserRole("11", createRole(orgId = "org1")),
            createUserRole("12", createRole(orgId = "org1")),
            /** 5th page from Cognito: No matches. */
            createUserRole("13", createRole(orgId = "org2")),
        )

    val cognitoClient =
        object : MockCognitoClient {
          /** To verify the number of requests made to Cognito. */
          val requestCountPerPaginationToken = mutableMapOf<String?, Int>()

          override fun listUsers(request: ListUsersRequest): ListUsersResponse {
            request.verify(
                expectedLimit = limit,
                // We verify pagination token below
                expectedPaginationToken = request.paginationToken(),
            )

            val (userRolesSlice: List<UserRole>, paginationToken: String?) =
                when (request.paginationToken()) {
                  null -> Pair(userRoles.slice(0..2), "token-1")
                  "token-1" -> Pair(userRoles.slice(3..5), "token-2")
                  "token-2" -> Pair(userRoles.slice(6..8), "token-3")
                  "token-3" -> Pair(userRoles.slice(9..11), "token-4")
                  "token-4" -> Pair(userRoles.slice(12..12), null)
                  else -> throw IllegalStateException()
                }

            this.requestCountPerPaginationToken.compute(
                request.paginationToken(),
            ) { _, existingValue ->
              (existingValue ?: 0) + 1
            }

            return ListUsersResponse.builder()
                .users(userRolesSlice.map { createCognitoUser(it.userId) })
                .paginationToken(paginationToken)
                .build()
          }
        }
    services.mockCognito(cognitoClient)
    userRoleRepo.batchCreate(userRoles)

    val results = ArrayList<UserList>()
    do {
      results.add(
          listUsers(
              limit = limit,
              filter = createUserFilter(orgId = "org1"),
              cursor = results.lastOrNull()?.nextCursor,
          )
      )
    } while (results.last().nextCursor != null)

    /**
     * We expect 2 requests with `token-1`, because we reached our limit halfway through that page,
     * so we'll have to refetch it on the next request.
     */
    cognitoClient.requestCountPerPaginationToken.shouldBe(
        mapOf(
            null to 1,
            "token-1" to 2,
            "token-2" to 1,
            "token-3" to 1,
            "token-4" to 1,
        )
    )

    val (result1, result2, result3, result4) = results.shouldHaveSize(4)

    result1.nextCursor.shouldBe(UserCursor("token-1", pageOffset = 1))
    result1.users
        .shouldHaveSize(limit)
        .shouldEqualRoles(listOf(userRoles[0], userRoles[2], userRoles[3]))

    result2.nextCursor.shouldBe(UserCursor("token-3", pageOffset = 0))
    result2.users
        .shouldHaveSize(limit)
        .shouldEqualRoles(listOf(userRoles[4], userRoles[6], userRoles[7]))

    result3.nextCursor.shouldBe(UserCursor("token-4", pageOffset = 0))
    result3.users.shouldHaveSize(limit).shouldEqualRoles(userRoles.slice(9..11))

    result4.nextCursor.shouldBeNull()
    result4.users.shouldBeEmpty()
  }
}

private fun createUserFilter(
    searchString: String? = null,
    searchField: UserSearchField? = null,
    orgId: String? = null,
    applicationName: String? = null,
    roleName: String? = null,
) =
    UserFilter(
        searchString = searchString,
        searchField = searchField,
        orgId = orgId,
        applicationName = applicationName,
        roleName = roleName,
    )

private fun List<UserDataWithRoles>.shouldEqualRoles(userRoles: List<UserRole>) {
  this.shouldHaveSize(userRoles.size)

  this.forEachIndexed { index, user ->
    val userRole = userRoles[index]

    user.username.shouldBe(userRole.userId)
    user.roles.shouldBe(userRole.roles)
  }
}
