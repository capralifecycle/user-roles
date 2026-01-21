package no.liflig.userroles.administration.identityprovider

import no.liflig.publicexception.ErrorCode
import no.liflig.publicexception.PublicException
import no.liflig.userroles.administration.CreateUserRequest
import no.liflig.userroles.administration.UpdateUserRequest
import no.liflig.userroles.administration.UserSearchField

/**
 * Fallback implementation of [IdentityProvider], that throws an exception on every method.
 *
 * Why? See [IdentityProvider.fromConfig]: We don't want the entire service to fail on startup if
 * the identity provider is misconfigured. So instead, we fall back to this implementation, which
 * will fail whenever the identity provider is accessed (in the user administration module), but
 * still allows for the rest of the service (namely role fetching for authentication) to keep
 * running.
 */
class MisconfiguredIdentityProvider : IdentityProvider {
  override val name = "misconfigured identity provider"

  private fun misconfiguredException(): Exception =
      PublicException(
          ErrorCode.INTERNAL_SERVER_ERROR,
          publicMessage = "User administration module not available",
          internalDetail =
              "There was an error when initializing the identity provider, see previous logs",
      )

  override fun getUser(username: String): UserDataWithoutRoles = throw misconfiguredException()

  override fun listUsers(
      limit: Int,
      cursor: String?,
      searchString: String?,
      searchField: UserSearchField?,
  ): IdentityProvider.UserList = throw misconfiguredException()

  override fun createUser(request: CreateUserRequest): UserDataWithoutRoles =
      throw misconfiguredException()

  override fun updateUser(request: UpdateUserRequest): Unit = throw misconfiguredException()

  override fun deleteUser(username: String): Unit = throw misconfiguredException()

  override fun resetUserPassword(username: String): Unit = throw misconfiguredException()
}
