package no.liflig.userroles.administration

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import no.liflig.publicexception.PublicException
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.NullSource
import org.junit.jupiter.params.provider.ValueSource
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient

class CognitoClientWrapperTest {
  @ParameterizedTest
  @ValueSource(strings = [""])
  @NullSource
  fun `initializing with null or blank userPoolId`(userPoolId: String?) {
    val wrapper = shouldNotThrowAny { CognitoClientWrapperImpl(userPoolId = userPoolId) }

    shouldThrow<PublicException> { wrapper.getOrThrow() }
  }

  @Test
  fun `cognito client initialization error`() {
    /**
     * Cognito client should fail to initialize, since we call
     * [CognitoIdentityProviderClient.create] without the required context in the environment.
     */
    val wrapper = shouldNotThrowAny { CognitoClientWrapperImpl(userPoolId = "test") }

    shouldThrow<PublicException> { wrapper.getOrThrow() }
  }
}
