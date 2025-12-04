package no.liflig.userroles.administration.api

import no.liflig.snapshot.verifyJsonSnapshot
import no.liflig.userroles.administration.MockCognitoClient
import no.liflig.userroles.administration.createCognitoUser
import no.liflig.userroles.roles.UserRole
import no.liflig.userroles.testutils.TestServices
import no.liflig.userroles.testutils.createRole
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersRequest
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersResponse

class UserAdministrationApiTest {
  @RegisterExtension private val services = TestServices.get()

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

    services.app.userRoleRepo.create(
        UserRole(
            userId = username,
            roles = listOf(createRole()),
        )
    )

    val response = services.listUsers(limit = 60)

    verifyJsonSnapshot(
        "administration/list-users-response.json",
        response.body.text,
    )
  }
}

private fun TestServices.listUsers(limit: Int): Response {
  return apiClient(
      Request(Method.GET, "${baseUrl}/api/administration/users")
          .query("limit", limit.toString())
          .withApiCredentials()
  )
}
