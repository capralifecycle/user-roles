package no.liflig.userroles.administration

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
              userId = number.toString(),
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
    services.app.userRoleRepo.batchCreate(userRoles)

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

    /**
     * Cognito client keeps a request count as state, in order to return new pages of users on each
     * new request.
     */
    val cognitoClient =
        object : MockCognitoClient {
          var requestCount = 0

          override fun listUsers(request: ListUsersRequest): ListUsersResponse {
            val (usernames: List<String>, paginationToken: String?) =
                when (this.requestCount) {
                  0 -> {
                    request.verify(expectedLimit = limit, expectedPaginationToken = null)
                    Pair(usernames.slice(0..2), "token-1")
                  }
                  1 -> {
                    request.verify(expectedLimit = limit, expectedPaginationToken = "token-1")
                    Pair(usernames.slice(3..5), "token-2")
                  }
                  2 -> {
                    request.verify(expectedLimit = limit, expectedPaginationToken = "token-2")
                    Pair(usernames.slice(6..7), null)
                  }
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
    services.app.userRoleRepo.batchCreate(usernames.map { createUserRole(it) })

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
    services.app.userRoleRepo.create(createUserRole())

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
                  userId = userCount.toString(),
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
    services.app.userRoleRepo.batchCreate(userRoles)

    /** Test filtering on each field by itself. */
    val filteredByApp = listUsers(filter = createUserFilter(applicationName = "app1")).users
    filteredByApp
        .shouldHaveSize(4)
        .shouldEqualRoles(userRoles.filter { it.roles.first().applicationName == "app1" })

    val filteredByOrg = listUsers(filter = createUserFilter(organizationId = "org1")).users
    filteredByOrg
        .shouldHaveSize(4)
        .shouldEqualRoles(userRoles.filter { it.roles.first().orgId == "org1" })

    val filteredByRole = listUsers(filter = createUserFilter(roleName = "role1")).users
    filteredByRole
        .shouldHaveSize(4)
        .shouldEqualRoles(userRoles.filter { it.roles.first().roleName == "role1" })

    /** Test filtering on 2 fields. */
    val filteredByAppAndOrg =
        listUsers(filter = createUserFilter(applicationName = "app2", organizationId = "org2"))
            .users
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
                        organizationId = "org1",
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
}

private const val DEFAULT_LIMIT = 20

private fun ListUsersRequest.verify(
    expectedLimit: Int = DEFAULT_LIMIT,
    expectedFilter: String? = null,
    expectedPaginationToken: String? = null,
    expectedUserPoolId: String = MockCognitoClient.USER_POOL_ID,
) {
  this.limit().shouldBe(expectedLimit)
  this.filter().shouldBe(expectedFilter)
  this.paginationToken().shouldBe(expectedPaginationToken)
  this.userPoolId().shouldBe(expectedUserPoolId)
}

private fun createUserFilter(
    searchString: String? = null,
    searchField: UserSearchField? = null,
    organizationId: String? = null,
    applicationName: String? = null,
    roleName: String? = null,
) =
    UserFilter(
        searchString = searchString,
        searchField = searchField,
        organizationId = organizationId,
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
