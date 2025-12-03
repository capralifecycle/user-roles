package no.liflig.userroles.administration

import no.liflig.logging.LogLevel
import no.liflig.logging.getLogger
import no.liflig.publicexception.ErrorCode
import no.liflig.publicexception.PublicException
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient

private val log = getLogger()

/**
 * Wraps a [CognitoIdentityProviderClient] and Cognito user pool ID. Catches config and
 * initialization errors, so that only modules that call [CognitoClientWrapper.getOrThrow] should be
 * affected. We do this so that the regular user roles module (used for authentication) can remain
 * up if the user administration module goes down.
 */
class CognitoClientWrapper(
    private val userPoolId: String?,
    /** Override for tests. */
    clientOverride: CognitoIdentityProviderClient?,
) {
  private val client: CognitoIdentityProviderClient? =
      try {
        when {
          clientOverride != null -> clientOverride
          userPoolId.isNullOrEmpty() -> {
            log.error {
              "Cognito user pool ID not configured. User administration module will be disabled, but fetching roles for authentication should still work"
            }
            null
          }
          else -> CognitoIdentityProviderClient.create()
        }
      } catch (e: Throwable) {
        log.error(e) {
          "Failed to initialize Cognito client. User administration module will be disabled, but fetching roles for authentication should still work"
        }
        null
      }

  /**
   * See [CognitoClientWrapper].
   *
   * @throws PublicException If the Cognito client or user pool ID are not available (either due to
   *   missing user pool config or an error during initialization). This maps to an HTTP 500
   *   Internal Server Error in our API setup, with a descriptive message for the client.
   */
  fun getOrThrow(): CognitoClientAndUserPoolId {
    if (client == null || userPoolId.isNullOrEmpty()) {
      throw PublicException(
          ErrorCode.INTERNAL_SERVER_ERROR,
          publicMessage = "User administration module not available",
          /**
           * If client/user pool ID is not configured, then we have already emitted an error log on
           * startup, so we can lower this severity to `WARN`.
           */
          severity = LogLevel.WARN,
      )
    }

    return CognitoClientAndUserPoolId(client, userPoolId)
  }
}

data class CognitoClientAndUserPoolId(
    val cognitoClient: CognitoIdentityProviderClient,
    val userPoolId: String,
)
