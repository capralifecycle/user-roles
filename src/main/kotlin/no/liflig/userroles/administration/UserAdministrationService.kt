package no.liflig.userroles.administration

import no.liflig.documentstore.entity.Versioned
import no.liflig.logging.field
import no.liflig.logging.getLogger
import no.liflig.publicexception.ErrorCode
import no.liflig.publicexception.PublicException
import no.liflig.userroles.administration.identityprovider.IdentityProvider
import no.liflig.userroles.administration.identityprovider.UserDataWithoutRoles
import no.liflig.userroles.roles.UserRole
import no.liflig.userroles.roles.UserRoleRepository

private val log = getLogger()

/**
 * Responsible for joining user data from our identity provider (AWS Cognito) with user roles stored
 * in this service.
 */
class UserAdministrationService(
    private val identityProvider: IdentityProvider,
    private val userRoleRepo: UserRoleRepository,
) {
  /** @throws PublicException To provide more context to the client about exactly what failed. */
  fun getUser(username: String): UserDataWithRoles {
    val (userRole, _) =
        userRoleRepo.getByUsername(username)
            ?: throw PublicException(
                ErrorCode.NOT_FOUND,
                publicMessage = "No roles found for user '${username}'",
            )

    val user: UserDataWithoutRoles =
        try {
          identityProvider.getUser(username)
        } catch (e: Exception) {
          throw PublicException(
              ErrorCode.INTERNAL_SERVER_ERROR,
              publicMessage =
                  "Failed to get user '${username}' from our identity provider (${identityProvider.name})",
              cause = e,
          )
        }

    return user.withRoles(userRole.roles)
  }

  /**
   * @param limit Number of users to return. Minimum 1, max 60.
   * @param cursor Null if this is the first fetch.
   * @throws PublicException To provide more context to the client about exactly what failed.
   */
  fun listUsers(
      limit: Int,
      cursor: UserCursor?,
      filter: UserFilter,
  ): UserList {
    val users = ArrayList<UserDataWithRoles>(limit)
    var fetchedFromIdentityProvider: Int
    var cursorFromIdentityProvider: String? = cursor?.cursorFromIdentityProvider
    var pageOffset = cursor?.pageOffset ?: 0

    /**
     * We run a loop here, because:
     * - User roles contain organization/application/role names, which clients want to filter on.
     * - But roles are stored in this service, so we can't filter by user role fields in the request
     *   to our identity provider.
     *     - And we can't first fetch from User Roles and _then_ from our identity provider, since
     *       Cognito's API does not allow us to efficiently fetch by a list of usernames.
     * - So in order to provide filtering on user role fields, we fetch without filtering from our
     *   identity provider, and then we do client-side filtering to omit users that do not match the
     *   given [UserFilter].
     *     - When users are filtered out, we have to fetch more pages from our identity provider
     *       until we fill our `limit`. So we run a loop until that is done (see exit condition at
     *       the end of the `do...while` loop below).
     */
    fetchLoop@ do {
      val userList: IdentityProvider.UserList =
          try {
            identityProvider.listUsers(
                limit = limit,
                cursor = cursorFromIdentityProvider,
                searchString = filter.searchString,
                searchField = filter.searchField,
            )
          } catch (e: Exception) {
            throw PublicException(
                ErrorCode.INTERNAL_SERVER_ERROR,
                publicMessage =
                    "Failed to fetch users from our identity provider (${identityProvider.name})",
                cause = e,
            )
          }

      fetchedFromIdentityProvider = userList.users.size
      if (fetchedFromIdentityProvider == 0) {
        break@fetchLoop
      }

      val usernames: List<String> = userList.users.map { it.username }
      val roles =
          try {
            userRoleRepo.listByUsernames(usernames)
          } catch (e: Exception) {
            throw PublicException(
                ErrorCode.INTERNAL_SERVER_ERROR,
                publicMessage = "Failed to retrieve roles for users",
                logFields = listOf(field("usernames", usernames)),
                cause = e,
            )
          }

      userLoop@ for ((index, user) in userList.users.asSequence().withIndex().drop(pageOffset)) {
        val userRole = findRolesForUser(user.username, roles) ?: continue@userLoop

        if (!filter.matches(userRole)) {
          continue@userLoop
        }

        /**
         * If we reach our `limit` halfway through the page, then we:
         * - Check if our [UserFilter] matches any of the remaining users in the page.
         * - If there are no matches in the rest of the page, then we just break out of the user
         *   loop and return the next identity provider cursor and `pageOffset = 0` as normal.
         * - But if there are matches in the rest of the page, then we have to continue our
         *   pagination on the next request from where we reached our `limit` here.
         *     - We do this by setting our pageOffset to where we are now in the page, so that we
         *       can return a cursor pointing to the middle of the page (see `UserCursor`).
         *     - Then we break out of the fetch loop, because in this case, we don't want to do any
         *       of the things we normally do below:
         *         - We don't want to add to any more to the users list
         *         - We don't want to update the identity provider cursor (since we need to fetch
         *           this same page again on the next request)
         *         - We don't want to reset the page offset
         */
        if (users.size == limit) {
          val hasMatchInRestOfPage =
              userList.users.asSequence().drop(index).any { remainingUser ->
                val userRole = findRolesForUser(remainingUser.username, roles) ?: return@any false
                return@any filter.matches(userRole)
              }

          if (hasMatchInRestOfPage) {
            pageOffset = index
            break@fetchLoop
          } else {
            break@userLoop
          }
        }

        users.add(user.withRoles(userRole.roles))
      }

      /**
       * Advance to the next cursor returned in the response from our identity provider. It may be
       * null, if this was the last page - in that case, we break out of the loop in the `while`
       * clause below.
       *
       * It's important that we do this here and not further up, since we don't always want to
       * advance the cursor (see comment on `users.size == limit` above).
       */
      cursorFromIdentityProvider = userList.nextCursor
      /** Reset page offset, so we start at beginning of next page. */
      pageOffset = 0
    } while (
        /**
         * We keep fetching pages while:
         * - The cursor returned by our identity provider is non-null, i.e. there are more pages to
         *   fetch
         * - The number of users fetched from our identity provider for the previous page is equal
         *   to the limit (if we received fewer than the limit, then this was the last page)
         * - The number of filtered users is less than the limit
         */
        cursorFromIdentityProvider != null &&
            fetchedFromIdentityProvider == limit &&
            users.size < limit
    )

    return UserList(
        users = users,
        nextCursor =
            cursorFromIdentityProvider?.let {
              UserCursor(cursorFromIdentityProvider = it, pageOffset = pageOffset)
            },
    )
  }

  /** @throws PublicException To provide more context to the client about exactly what failed. */
  fun createUser(request: CreateUserRequest): UserDataWithRoles {
    val userRole: Versioned<UserRole> =
        try {
          userRoleRepo.create(
              UserRole(username = request.user.username, roles = request.user.roles)
          )
        } catch (e: Exception) {
          if (UserRoleRepository.isDuplicateUsernameException(e)) {
            throw PublicException(
                ErrorCode.CONFLICT,
                publicMessage = "Failed to create roles for user",
                publicDetail = "Roles already exist for username '${request.user.username}'",
                internalDetail =
                    "This may be because roles have been created for a user, but the user is not registered in our identity provider (${identityProvider.name}) yet",
                cause = e,
            )
          } else {
            throw PublicException(
                ErrorCode.INTERNAL_SERVER_ERROR,
                publicMessage = "Failed to create roles for user",
                cause = e,
            )
          }
        }

    val user: UserDataWithoutRoles =
        try {
          identityProvider.createUser(request)
        } catch (e: Exception) {
          /**
           * If we fail to create the user in our identity provider, we also want to delete the user
           * role we created.
           */
          try {
            userRoleRepo.delete(userRole)
          } catch (deleteException: Exception) {
            log.error(deleteException) {
              field("username", request.user.username)
              "Failed to clean up created user role after user registration failed"
            }
          }

          throw PublicException(
              ErrorCode.INTERNAL_SERVER_ERROR,
              publicMessage =
                  "Failed to register user in our identity provider (${identityProvider.name})",
              cause = e,
          )
        }

    return user.withRoles(userRole.data.roles)
  }

  /**
   * See [IdentityProvider.updateUser] for why this returns nothing.
   *
   * @throws PublicException To provide more context to the client about exactly what failed.
   */
  fun updateUser(request: UpdateUserRequest) {
    val previousUserRole: Versioned<UserRole> =
        userRoleRepo.getByUsername(request.user.username)
            ?: throw PublicException(
                ErrorCode.NOT_FOUND,
                publicMessage = "Failed to update user '${request.user.username}'",
                publicDetail = "No roles found for user",
            )
    var updatedUserRole: Versioned<UserRole>? = null

    if (previousUserRole.data.roles != request.user.roles) {
      try {
        updatedUserRole =
            userRoleRepo.update(previousUserRole.map { it.copy(roles = request.user.roles) })
      } catch (e: Exception) {
        throw PublicException(
            ErrorCode.INTERNAL_SERVER_ERROR,
            publicMessage = "Failed to update roles for user",
            cause = e,
        )
      }
    }

    try {
      identityProvider.updateUser(request)
    } catch (e: Exception) {
      /**
       * If we fail to update the user in our identity provider, we want to revert the user role
       * update.
       */
      if (updatedUserRole != null) {
        try {
          userRoleRepo.update(previousUserRole.data, updatedUserRole.version)
        } catch (e: Exception) {
          log.error(e) {
            field("username", request.user.username)
            "Failed to revert user role update after user attribute update failed"
          }
        }
      }

      throw PublicException(
          ErrorCode.INTERNAL_SERVER_ERROR,
          publicMessage =
              "Failed to update user attributes in our identity provider (${identityProvider.name})",
          cause = e,
      )
    }
  }

  /** @throws PublicException To provide more context to the client about exactly what failed. */
  fun deleteUser(username: String) {
    try {
      identityProvider.deleteUser(username)
    } catch (e: Exception) {
      throw PublicException(
          ErrorCode.INTERNAL_SERVER_ERROR,
          publicMessage =
              "Failed to delete user data in our identity provider (${identityProvider.name})",
          cause = e,
      )
    }

    userRoleRepo.transactional {
      val userRole = userRoleRepo.getByUsername(username)
      if (userRole == null) {
        /**
         * We expect the user to have an associated user role. If they don't, we log an error, but
         * the request can still succeed.
         */
        log.error {
          field("username", username)
          "Successfully deleted user data in our identity provider (${identityProvider.name}), but failed to find roles associated with the user"
        }
        return
      }

      try {
        userRoleRepo.delete(userRole)
      } catch (e: Exception) {
        throw PublicException(
            ErrorCode.INTERNAL_SERVER_ERROR,
            publicMessage =
                "Successfully deleted user data in our identity provider (${identityProvider.name}), but failed to delete the roles associated with the user",
            publicDetail = "Manual cleanup by a system administrator may be required",
            cause = e,
        )
      }
    }
  }

  /**
   * After this is called, a password-reset code will be sent to the user (provided they have a
   * verified email or phone number registered).
   *
   * @throws PublicException To provide more context to the client about what failed.
   */
  fun resetUserPassword(username: String) {
    try {
      identityProvider.resetUserPassword(username)
    } catch (e: Exception) {
      throw PublicException(
          ErrorCode.INTERNAL_SERVER_ERROR,
          publicMessage =
              "Failed to reset user password in our identity provider (${identityProvider.name})",
          cause = e,
      )
    }
  }

  private fun findRolesForUser(
      username: String,
      userRoles: List<Versioned<UserRole>>,
  ): UserRole? {
    return userRoles.find { it.data.username == username }?.data
        ?: run {
          /**
           * If we find users in our identity provider without associated roles, then something has
           * gone awry (or someone has gone around the user administration API). This is likely a
           * mistake, so we log this at ERROR (we may consider lowering this to WARN if it becomes
           * annoying).
           */
          log.error {
            field("username", username)
            "Found user in our identity provider (${identityProvider.name}) without associated user roles"
          }
          return null
        }
  }
}

data class UserList(
    val users: List<UserDataWithRoles>,
    /** See [UserCursor]. Null if there are no more users to fetch. */
    val nextCursor: UserCursor?,
)
