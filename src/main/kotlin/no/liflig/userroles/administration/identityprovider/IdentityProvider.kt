package no.liflig.userroles.administration.identityprovider

import no.liflig.logging.getLogger
import no.liflig.userroles.administration.CreateUserRequest
import no.liflig.userroles.administration.UpdateUserRequest
import no.liflig.userroles.administration.UserAdministrationService
import no.liflig.userroles.administration.UserDataWithRoles
import no.liflig.userroles.administration.UserSearchField
import no.liflig.userroles.administration.identityprovider.cognito.CognitoIdentityProvider
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient

private val log = getLogger()

/**
 * A service for managing users (e.g. AWS Cognito).
 *
 * An identity provider provides all the user data in [UserDataWithRoles], except for the roles
 * (since those are stored in this service). The [UserAdministrationService] does the work of
 * joining identity provider user data together with the user's roles.
 */
interface IdentityProvider {
  /** Human-readable name of the identity provide service, e.g. "AWS Cognito". */
  val name: String

  fun getUser(username: String): UserDataWithoutRoles

  fun listUsers(
      limit: Int,
      cursor: String?,
      searchString: String?,
      searchField: UserSearchField?,
  ): UserList

  data class UserList(
      val users: List<UserDataWithoutRoles>,
      /**
       * Pagination cursor to get the next page of users on a subsequent request. In AWS Cognito,
       * this is called a "PaginationToken".
       *
       * Should be `null` when there are no more pages to fetch.
       */
      val nextCursor: String?,
  )

  /** The returned user's roles will be empty (must be joined with roles after fetching). */
  fun createUser(request: CreateUserRequest): UserDataWithoutRoles

  /**
   * Returns nothing, because Cognito's `AdminUpdateUserAttributes` does not return anything on
   * success. We _could_ fetch the user from Cognito after updating, but that contributes to the
   * Cognito request limit, which we may not want.
   */
  fun updateUser(request: UpdateUserRequest)

  fun deleteUser(username: String)

  fun resetUserPassword(username: String)

  companion object {
    /**
     * Attempts to initialize a [CognitoIdentityProvider] from the given config parameters. If
     * initialization fails, we fall back to [MisconfiguredIdentityProvider]. This means that the
     * user administration module will be unavailable, but the rest of the service should still
     * start up and be able to respond to user role fetch requests for authentication.
     *
     * If we want to support different identity provider implementations than Cognito in the future,
     * this is where we would choose which implementation to initialize.
     */
    fun fromConfig(cognitoUserPoolId: String?): IdentityProvider {
      try {
        if (cognitoUserPoolId.isNullOrEmpty()) {
          log.error {
            "Cognito user pool ID not configured. User administration module will be disabled, but fetching roles for authentication should still work"
          }
        } else {
          return CognitoIdentityProvider(
              cognitoClient = CognitoIdentityProviderClient.create(),
              userPoolId = cognitoUserPoolId,
          )
        }
      } catch (e: Exception) {
        log.error(e) {
          "Failed to initialize Cognito client. User administration module will be disabled, but fetching roles for authentication should still work"
        }
      }

      return MisconfiguredIdentityProvider()
    }
  }
}
