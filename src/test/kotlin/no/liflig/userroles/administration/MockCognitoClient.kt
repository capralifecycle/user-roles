package no.liflig.userroles.administration

import java.time.Instant
import no.liflig.userroles.testutils.DEFAULT_TEST_USERNAME
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType
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
    attributes: Map<String, String> = emptyMap(),
): UserType {
  val attributeList =
      attributes.mapTo(ArrayList()) { (key, value) ->
        AttributeType.builder().name(key).value(value).build()
      }

  /** sub (user ID) is required. */
  if (!attributes.containsKey("sub")) {
    attributeList.add(
        AttributeType.builder().name("sub").value("4b670e7f-0ae9-4ce8-9a8b-b27d00d2f31d").build()
    )
  }

  return UserType.builder()
      .username(username)
      .userStatus(status)
      .userCreateDate(createDate)
      .enabled(enabled)
      .attributes(attributeList)
      .build()
}
