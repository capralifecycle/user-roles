package no.liflig.userroles.administration

import java.time.Instant
import no.liflig.userroles.testutils.DEFAULT_TEST_USERNAME
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserStatusType
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserType

class MockCognitoClientWrapper : CognitoClientWrapper {
  var cognitoClient: CognitoIdentityProviderClient? = null

  override fun getOrThrow() =
      CognitoClientAndUserPoolId(cognitoClient!!, MockCognitoClient.USER_POOL_ID)

  fun reset() {
    cognitoClient = null
  }
}

/** Base interface for mocking Cognito. */
interface MockCognitoClient : CognitoIdentityProviderClient {
  override fun serviceName(): String = "mock-cognito-client"

  override fun close() {}

  companion object {
    const val USER_POOL_ID = "test-user-pool-id"
  }
}

fun createCognitoUser(
    username: String = DEFAULT_TEST_USERNAME,
    status: UserStatusType = UserStatusType.CONFIRMED,
    createDate: Instant = Instant.parse("2025-12-04T07:25:11Z"),
    enabled: Boolean = true,
): UserType {
  return UserType.builder()
      .username(username)
      .userStatus(status)
      .userCreateDate(createDate)
      .enabled(enabled)
      .build()
}
