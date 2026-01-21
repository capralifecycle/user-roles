package no.liflig.userroles.testutils

import no.liflig.userroles.administration.CreateUserRequest
import no.liflig.userroles.administration.UpdateUserRequest
import no.liflig.userroles.administration.UserSearchField
import no.liflig.userroles.administration.identityprovider.IdentityProvider
import no.liflig.userroles.administration.identityprovider.MisconfiguredIdentityProvider
import no.liflig.userroles.administration.identityprovider.UserDataWithoutRoles

/**
 * [IdentityProvider] implementation for tests, that delegates to another identity provider
 * implementation, but allows us to change the implementation while the app is still running.
 */
class DelegatingIdentityProvider : IdentityProvider {
  var delegate: IdentityProvider = MisconfiguredIdentityProvider()

  fun reset() {
    this.delegate = MisconfiguredIdentityProvider()
  }

  override val name: String
    get() = delegate.name

  override fun getUser(username: String): UserDataWithoutRoles = delegate.getUser(username)

  override fun listUsers(
      limit: Int,
      cursor: String?,
      searchString: String?,
      searchField: UserSearchField?,
  ): IdentityProvider.UserList =
      delegate.listUsers(
          limit = limit,
          cursor = cursor,
          searchString = searchString,
          searchField = searchField,
      )

  override fun createUser(request: CreateUserRequest): UserDataWithoutRoles =
      delegate.createUser(request)

  override fun updateUser(request: UpdateUserRequest) = delegate.updateUser(request)

  override fun deleteUser(username: String) = delegate.deleteUser(username)

  override fun resetUserPassword(username: String) = delegate.resetUserPassword(username)
}
