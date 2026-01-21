package no.liflig.userroles.administration.api

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldBeEmpty
import java.time.Instant
import no.liflig.snapshot.verifyJsonSnapshot
import no.liflig.userroles.administration.COGNITO_CUSTOM_ATTRIBUTE_PREFIX
import no.liflig.userroles.administration.CreateUserRequest
import no.liflig.userroles.administration.InvitationMessageType
import no.liflig.userroles.administration.MockCognitoClient
import no.liflig.userroles.administration.StandardAttribute
import no.liflig.userroles.administration.UpdateUserRequest
import no.liflig.userroles.administration.UserAdministrationService
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
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserRequest
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserResponse
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminResetUserPasswordRequest
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminResetUserPasswordResponse
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminUpdateUserAttributesRequest
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminUpdateUserAttributesResponse
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType
import software.amazon.awssdk.services.cognitoidentityprovider.model.DeliveryMediumType
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersRequest
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersResponse
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserStatusType

class UserAdministrationApiTest {
  @RegisterExtension private val services = TestServices.get()
  private val userRoleRepo = services.app.userRoleRepo

  companion object {
    val TEST_USER =
        EXAMPLE_USER_UPDATE_DATA.let {
          it.copy(
              /** To test custom attributes (see [COGNITO_CUSTOM_ATTRIBUTE_PREFIX]). */
              attributes = it.attributes + mapOf("astrologicalSign" to "Libra"),
          )
        }
    val TEST_COGNITO_USER =
        createCognitoUser(
            username = "test.testesen",
            attributes =
                listOf(
                    createAttribute(StandardAttribute.EMAIL, "test@example.org"),
                    createAttribute(StandardAttribute.EMAIL_VERIFIED, "true"),
                    createAttribute(StandardAttribute.PHONE_NUMBER, "12345678"),
                    createAttribute(StandardAttribute.PHONE_NUMBER_VERIFIED, "false"),
                    /** "name" is a standard attribute, so no "custom:" prefix. */
                    AttributeType.builder().name("name").value("Test Testesen").build(),
                    /** Custom attributes have "custom:" prefix. */
                    AttributeType.builder().name("custom:astrologicalSign").value("Libra").build(),
                ),
        )
  }

  @Test
  fun `get user`() {
    val user = TEST_COGNITO_USER

    services.mockCognito(
        object : MockCognitoClient {
          override fun adminGetUser(request: AdminGetUserRequest): AdminGetUserResponse {
            return AdminGetUserResponse.builder()
                .username(user.username())
                .userStatus(user.userStatus())
                .userCreateDate(user.userCreateDate())
                .enabled(user.enabled())
                .userAttributes(user.attributes())
                .build()
          }
        }
    )

    userRoleRepo.create(UserRole(username = user.username(), roles = listOf(createRole())))

    val response = services.sendGetUserRequest(username = user.username())
    response.status.shouldBe(Status.OK)

    verifyJsonSnapshot(
        "administration/get-user-response.json",
        response.body.text,
    )
  }

  @Test
  fun `list users`() {
    val cognitoUser = TEST_COGNITO_USER

    services.mockCognito(
        object : MockCognitoClient {
          override fun listUsers(req: ListUsersRequest) =
              ListUsersResponse.builder()
                  .users(cognitoUser)
                  .paginationToken("test-pagination-token")
                  .build()
        }
    )

    userRoleRepo.create(UserRole(username = cognitoUser.username(), roles = listOf(createRole())))

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
    val user = TEST_USER
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
                      /** "name" is a standard attribute, so no "custom:" prefix. */
                      AttributeType.builder().name("name").value("Test Testesen").build(),
                      /** Custom attributes have "custom:" prefix. */
                      AttributeType.builder()
                          .name("custom:astrologicalSign")
                          .value("Libra")
                          .build(),
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

    val userRole = userRoleRepo.getByUsername(user.username).shouldNotBeNull()
    userRole.data.roles.shouldBe(user.roles)

    verifyJsonSnapshot(
        "administration/create-user-response.json",
        response.body.text,
    )
  }

  @Test
  fun `update user`() {
    val username = TEST_USER.username
    val existingUserRole =
        userRoleRepo.create(
            createUserRole(
                username,
                createRole(roleName = "member", orgId = "org1", applicationName = "app1"),
            ),
        )
    val updatedUser =
        TEST_USER.copy(
            /**
             * Set `phoneNumber = null`, to test that we map it to blank values in our request to
             * Cognito (see [UpdateUserRequest.toCognitoRequest]).
             */
            phoneNumber = null,
            /** Promote to admin in app1, add member role in app2. */
            roles =
                listOf(
                    createRole(roleName = "admin", orgId = "org1", applicationName = "app1"),
                    createRole(roleName = "member", orgId = "org1", applicationName = "app2"),
                ),
        )
    updatedUser.roles.shouldNotBe(existingUserRole.data.roles)

    val cognitoClient =
        object : MockCognitoClient {
          var requestCount = 0

          override fun adminUpdateUserAttributes(
              request: AdminUpdateUserAttributesRequest
          ): AdminUpdateUserAttributesResponse {
            request.should {
              it.username().shouldBe(updatedUser.username)
              it.userPoolId().shouldBe(MockCognitoClient.USER_POOL_ID)
              it.userAttributes()
                  .shouldContainExactlyInAnyOrder(
                      createAttribute(StandardAttribute.EMAIL, updatedUser.email!!.value),
                      createAttribute(
                          StandardAttribute.EMAIL_VERIFIED,
                          updatedUser.email.verified.toString(),
                      ),
                      createAttribute(StandardAttribute.PHONE_NUMBER, value = ""),
                      createAttribute(StandardAttribute.PHONE_NUMBER_VERIFIED, value = ""),
                      /** "name" is a standard attribute, so no "custom:" prefix. */
                      AttributeType.builder().name("name").value("Test Testesen").build(),
                      /** Custom attributes have "custom:" prefix. */
                      AttributeType.builder()
                          .name("custom:astrologicalSign")
                          .value("Libra")
                          .build(),
                  )
            }

            requestCount++

            return AdminUpdateUserAttributesResponse.builder().build()
          }
        }
    services.mockCognito(cognitoClient)

    val response = services.sendUpdateUserRequest(UpdateUserRequest(updatedUser))
    response.status.shouldBe(Status.OK)
    /** We expect empty response body here (see [UserAdministrationService.updateUser]). */
    response.body.text.shouldBeEmpty()

    cognitoClient.requestCount.shouldBe(1)

    val updatedUserRole = userRoleRepo.getByUsername(username).shouldNotBeNull()
    updatedUserRole.data.roles.shouldBe(updatedUser.roles)
  }

  @Test
  fun `delete user`() {
    val userRole = createUserRole(username = DEFAULT_TEST_USERNAME, createRole(orgId = "org1"))

    val cognitoClient =
        object : MockCognitoClient {
          var requestCount = 0

          override fun adminDeleteUser(request: AdminDeleteUserRequest): AdminDeleteUserResponse {
            request.username().shouldNotBeNull().shouldBe(userRole.username)
            request.userPoolId().shouldBe(MockCognitoClient.USER_POOL_ID)

            requestCount++

            return AdminDeleteUserResponse.builder().build()
          }
        }
    services.mockCognito(cognitoClient)
    userRoleRepo.create(userRole)

    val response = services.sendDeleteUserRequest(username = userRole.username)
    response.status.shouldBe(Status.OK)
    response.body.text.shouldBeEmpty()

    cognitoClient.requestCount.shouldBe(1)
  }

  @Test
  fun `reset user password`() {
    val username = DEFAULT_TEST_USERNAME

    val cognitoClient =
        object : MockCognitoClient {
          var requestCount = 0

          override fun adminResetUserPassword(
              request: AdminResetUserPasswordRequest
          ): AdminResetUserPasswordResponse {
            request.username().shouldBe(username)
            request.userPoolId().shouldBe(MockCognitoClient.USER_POOL_ID)

            requestCount++

            return AdminResetUserPasswordResponse.builder().build()
          }
        }
    services.mockCognito(cognitoClient)

    val response = services.sendResetUserPasswordRequest(username = username)
    response.status.shouldBe(Status.OK)
    response.body.text.shouldBeEmpty()

    cognitoClient.requestCount.shouldBe(1)
  }
}

private fun TestServices.sendGetUserRequest(username: String): Response {
  return apiClient(
      Request(Method.GET, "${baseUrl}/api/administration/users/${username}").withApiCredentials(),
  )
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

private fun TestServices.sendUpdateUserRequest(body: UpdateUserRequest): Response {
  return apiClient(
      Request(Method.PUT, "${baseUrl}/api/administration/users")
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

private fun TestServices.sendResetUserPasswordRequest(username: String): Response {
  return apiClient(
      Request(Method.POST, "${baseUrl}/api/administration/users/${username}/reset-password")
          .withApiCredentials(),
  )
}
