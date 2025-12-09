package no.liflig.userroles.administration.api

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldBeEmpty
import java.time.Instant
import no.liflig.snapshot.verifyJsonSnapshot
import no.liflig.userroles.administration.CreateUserRequest
import no.liflig.userroles.administration.InvitationMessageType
import no.liflig.userroles.administration.MockCognitoClient
import no.liflig.userroles.administration.StandardAttribute
import no.liflig.userroles.administration.UserCursor
import no.liflig.userroles.administration.UserFilter
import no.liflig.userroles.administration.UserSearchField
import no.liflig.userroles.administration.createAttribute
import no.liflig.userroles.administration.createCognitoUser
import no.liflig.userroles.common.serialization.json
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
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserRequest
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserResponse
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminDeleteUserRequest
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminDeleteUserResponse
import software.amazon.awssdk.services.cognitoidentityprovider.model.DeliveryMediumType
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersRequest
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersResponse
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserStatusType

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
                              listOf(
                                  createAttribute(StandardAttribute.EMAIL, "test@example.org"),
                                  createAttribute(StandardAttribute.EMAIL_VERIFIED, "true"),
                                  createAttribute(StandardAttribute.PHONE_NUMBER, "12345678"),
                                  createAttribute(StandardAttribute.PHONE_NUMBER_VERIFIED, "true"),
                                  createAttribute("name", "Test Testesen"),
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
  fun `create user`() {
    val user = EXAMPLE_USER_UPDATE_DATA
    val request =
        CreateUserRequest(
            user = user,
            invitationMessages = setOf(InvitationMessageType.EMAIL, InvitationMessageType.SMS),
        )

    val userStatus = "FORCE_CHANGE_PASSWORD"
    val userCreateDate = Instant.parse("2025-12-08T14:36:03Z")
    val userId = "5cb274a4-3281-4560-86f3-d32d68849b0d"

    val cognitoClient =
        object : MockCognitoClient {
          var requestCount = 0

          override fun adminCreateUser(
              cognitoRequest: AdminCreateUserRequest
          ): AdminCreateUserResponse {
            cognitoRequest.should {
              it.username().shouldBe(user.username)
              it.userPoolId().shouldBe(MockCognitoClient.USER_POOL_ID)
              it.userAttributes()
                  .shouldContainExactlyInAnyOrder(
                      createAttribute(StandardAttribute.EMAIL, user.email!!.value),
                      createAttribute(
                          StandardAttribute.EMAIL_VERIFIED,
                          user.email.verified.toString(),
                      ),
                      createAttribute(StandardAttribute.PHONE_NUMBER, user.phoneNumber!!.value),
                      createAttribute(
                          StandardAttribute.PHONE_NUMBER_VERIFIED,
                          user.phoneNumber.verified.toString(),
                      ),
                      createAttribute("name", "Test Testesen"),
                  )
              it.desiredDeliveryMediums()
                  .shouldContainExactlyInAnyOrder(DeliveryMediumType.EMAIL, DeliveryMediumType.SMS)
            }

            requestCount++

            return AdminCreateUserResponse.builder()
                .user(
                    createCognitoUser(
                        username = user.username,
                        status = UserStatusType.fromValue(userStatus),
                        createDate = userCreateDate,
                        attributes = cognitoRequest.userAttributes(),
                        userId = userId,
                        enabled = true,
                    )
                )
                .build()
          }
        }
    services.mockCognito(cognitoClient)

    val response = services.sendCreateUserRequest(request)
    response.status.shouldBe(Status.OK)

    cognitoClient.requestCount.shouldBe(1)

    val userRole = userRoleRepo.getByUserId(user.username).shouldNotBeNull()
    userRole.data.roles.shouldBe(user.roles)

    verifyJsonSnapshot(
        "administration/create-user-response.json",
        response.body.text,
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

private fun TestServices.sendCreateUserRequest(body: CreateUserRequest): Response {
  return apiClient(
      Request(Method.POST, "${baseUrl}/api/administration/users")
          .body(json.encodeToString(body))
          .withApiCredentials(),
  )
}

private fun TestServices.sendDeleteUserRequest(username: String): Response {
  return apiClient(
      Request(Method.DELETE, "${baseUrl}/api/administration/users/${username}")
          .withApiCredentials(),
  )
}
