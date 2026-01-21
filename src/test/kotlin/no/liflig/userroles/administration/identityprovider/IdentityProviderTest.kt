package no.liflig.userroles.administration.identityprovider

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import no.liflig.publicexception.PublicException
import no.liflig.userroles.administration.UserAdministrationServiceTest
import no.liflig.userroles.administration.api.UserAdministrationApiTest
import no.liflig.userroles.administration.identityprovider.cognito.CognitoIdentityProvider
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.NullSource
import org.junit.jupiter.params.provider.ValueSource
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient

/**
 * The [CognitoIdentityProvider] is already covered by tests in [UserAdministrationServiceTest] and
 * [UserAdministrationApiTest]. So here we test that invalid configuration of [IdentityProvider]
 * gracefully falls back to [MisconfiguredIdentityProvider] without throwing an exception.
 */
class IdentityProviderTest {
  @ParameterizedTest
  @ValueSource(strings = [""])
  @NullSource
  fun `initializing with null or blank userPoolId`(userPoolId: String?) {
    val identityProvider = shouldNotThrowAny {
      IdentityProvider.fromConfig(cognitoUserPoolId = userPoolId)
    }
    identityProvider.shouldBeInstanceOf<MisconfiguredIdentityProvider>()

    val exception = shouldThrow<PublicException> { identityProvider.getUser("user") }
    exception.publicMessage.shouldBe("User administration module not available")
  }

  @Test
  fun `cognito client initialization error`() {
    /**
     * Cognito client should fail to initialize, since we call
     * [CognitoIdentityProviderClient.create] without the required context in the environment.
     */
    val identityProvider = shouldNotThrowAny {
      IdentityProvider.fromConfig(cognitoUserPoolId = "test")
    }
    identityProvider.shouldBeInstanceOf<MisconfiguredIdentityProvider>()

    val exception = shouldThrow<PublicException> { identityProvider.getUser("user") }
    exception.publicMessage.shouldBe("User administration module not available")
  }
}
