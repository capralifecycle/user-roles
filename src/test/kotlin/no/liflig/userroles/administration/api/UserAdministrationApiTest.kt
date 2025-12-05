package no.liflig.userroles.administration.api

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldBeEmpty
import no.liflig.snapshot.verifyJsonSnapshot
import no.liflig.userroles.administration.MockCognitoClient
import no.liflig.userroles.administration.UserCursor
import no.liflig.userroles.administration.UserFilter
import no.liflig.userroles.administration.UserSearchField
import no.liflig.userroles.administration.createCognitoUser
import no.liflig.userroles.roles.UserRole
import no.liflig.userroles.testutils.DEFAULT_TEST_USERNAME
import no.liflig.userroles.testutils.TestServices
import no.liflig.userroles.testutils.createRole
import no.liflig.userroles.testutils.createUserRole
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminDeleteUserRequest
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminDeleteUserResponse
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersRequest
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersResponse

class UserAdministrationApiTest {
  @RegisterExtension private val services = TestServices.get()
  private val userRoleRepo = services.app.userRoleRepo

  @Test
  fun `list users`() {
    val username = "test.testesen"

    services.mockCognito(
        object : MockCognitoClient {
          override fun listUsers(req: ListUsersRequest) =
              ListUsersResponse.builder()
                  .users(
                      createCognitoUser(
                          username,
                          attributes =
                              mapOf(
                                  "email" to "test@example.org",
                                  "email_verified" to "true",
                                  "phone_number" to "12345678",
                                  "phone_number_verified" to "true",
                                  "name" to "Test Testesen",
                              ),
                      )
                  )
                  .paginationToken("test-pagination-token")
                  .build()
        }
    )

    userRoleRepo.create(UserRole(userId = username, roles = listOf(createRole())))

    val response = services.sendListUsersRequest(limit = 60)

    verifyJsonSnapshot(
        "administration/list-users-response.json",
        response.body.text,
    )
  }

  @Test
  fun `query parameters are mapped as expected`() {
    val request =
        Request(Method.GET, "${services.baseUrl}/api/administration/users")
            .query("limit", "20")
            .query("searchString", "test")
            .query("searchField", "USERNAME")
            .query("orgId", "org1")
            .query("applicationName", "app1")
            .query("roleName", "role1")
            .query("cursor", "test-pagination-token___10")

    ListUsersEndpoint.getLimitFromRequest(request).shouldBe(20)

    ListUsersEndpoint.getUserFilterFromRequest(request)
        .shouldBe(
            UserFilter(
                searchString = "test",
                searchField = UserSearchField.USERNAME,
                orgId = "org1",
                applicationName = "app1",
                roleName = "role1",
            )
        )

    ListUsersEndpoint.getCursorFromRequest(request, limit = 20)
        .shouldBe(
            UserCursor(cognitoPaginationToken = "test-pagination-token", pageOffset = 10),
        )
  }

  @Test
  fun `delete user`() {
    val userRole = createUserRole(username = DEFAULT_TEST_USERNAME, createRole(orgId = "org1"))

    val cognitoClient =
        object : MockCognitoClient {
          var requestCount = 0

          override fun adminDeleteUser(request: AdminDeleteUserRequest): AdminDeleteUserResponse {
            request.username().shouldNotBeNull().shouldBe(userRole.userId)
            request.userPoolId().shouldBe(MockCognitoClient.USER_POOL_ID)

            requestCount++

            return AdminDeleteUserResponse.builder().build()
          }
        }
    services.mockCognito(cognitoClient)
    userRoleRepo.create(userRole)

    val response = services.sendDeleteUserRequest(username = userRole.userId)
    response.status.shouldBe(Status.OK)
    response.body.text.shouldBeEmpty()

    cognitoClient.requestCount.shouldBe(1)
  }
}

private fun TestServices.sendListUsersRequest(limit: Int): Response {
  return apiClient(
      Request(Method.GET, "${baseUrl}/api/administration/users")
          .query("limit", limit.toString())
          .withApiCredentials()
  )
}

private fun TestServices.sendDeleteUserRequest(username: String): Response {
  return apiClient(
      Request(Method.DELETE, "${baseUrl}/api/administration/users/${username}")
          .withApiCredentials(),
  )
}
