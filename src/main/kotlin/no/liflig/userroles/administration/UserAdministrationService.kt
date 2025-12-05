package no.liflig.userroles.administration

import no.liflig.documentstore.entity.Versioned
import no.liflig.logging.field
import no.liflig.logging.getLogger
import no.liflig.publicexception.ErrorCode
import no.liflig.publicexception.PublicException
import no.liflig.userroles.roles.UserRole
import no.liflig.userroles.roles.UserRoleRepository
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserType

private val log = getLogger()

class UserAdministrationService(
    private val userRoleRepo: UserRoleRepository,
    private val cognitoClientWrapper: CognitoClientWrapper,
) {
  /**
   * @param limit Number of users to return. Minimum 1, max 60.
   * @param cursor Null if this is the first fetch.
   * @throws PublicException To provide more context to the client about exactly what failed.
   */
  fun listUsers(
      limit: Int,
      filter: UserFilter,
      cursor: UserCursor?,
  ): UserList {
    /**
     * We only require initialization of the Cognito client on-demand here, so that if there's an
     * error in our Cognito setup, then only the user administration module is affected (not the
     * regular user roles module used for authentication).
     */
    val (cognitoClient, userPoolId) = cognitoClientWrapper.getOrThrow()

    val users = ArrayList<UserDataWithRoles>(limit)
    var fetchedFromCognito: Int
    var cognitoPaginationToken: String? = cursor?.cognitoPaginationToken
    var pageOffset = cursor?.pageOffset ?: 0
    val cognitoFilterString = filter.getCognitoFilterString()

    /**
     * We run a loop here, because:
     * - User roles contain organization/application/role names, which clients want to filter on.
     * - But roles are stored in this service, so we can't filter by user role fields in our request
     *   to Cognito.
     *     - And we can't first fetch from User Roles and _then_ from Cognito, since Cognito's API
     *       does not allow us to efficiently fetch by a list of usernames.
     * - So in order to provide filtering on user role fields, we fetch without filtering from
     *   Cognito, and then we do client-side filtering to omit users that do not match the given
     *   [UserFilter].
     *     - When users are filtered out, we have to fetch more pages from Cognito until we fill our
     *       `limit`. So we run a loop until that is done (see exit condition at the end of the
     *       `do...while` loop below).
     */
    fetchLoop@ do {
      val cognitoResponse =
          try {
            cognitoClient.listUsers { request ->
              request.limit(limit).userPoolId(userPoolId)
              cognitoPaginationToken?.let { request.paginationToken(it) }
              cognitoFilterString?.let { request.filter(it) }
            }
          } catch (e: Exception) {
            throw PublicException(
                ErrorCode.INTERNAL_SERVER_ERROR,
                publicMessage = "Failed to fetch users from our identity provider (Cognito)",
                cause = e,
            )
          }

      fetchedFromCognito = cognitoResponse.users().size
      if (fetchedFromCognito == 0) {
        break@fetchLoop
      }

      val usernames: List<String> = cognitoResponse.users().map { it.username() }
      val roles =
          try {
            userRoleRepo.listByUserIds(userIds = cognitoResponse.users().map { it.username() })
          } catch (e: Exception) {
            throw PublicException(
                ErrorCode.INTERNAL_SERVER_ERROR,
                publicMessage = "Failed to retrieve roles for users",
                logFields = listOf(field("usernames", usernames)),
                cause = e,
            )
          }

      userLoop@ for ((index, cognitoUser) in
          cognitoResponse.users().asSequence().withIndex().drop(pageOffset)) {
        val userRole = findRolesForUser(cognitoUser, roles) ?: continue@userLoop

        if (!filter.matches(userRole)) {
          continue@userLoop
        }

        /**
         * If we reach our `limit` halfway through the page, then we:
         * - Check if our [UserFilter] matches any of the remaining users in the page.
         * - If there are no matches in the rest of the page, then we just break out of the user
         *   loop and return the next Cognito pagination token and `pageOffset = 0` as normal.
         * - But if there are matches in the rest of the page, then we have to continue our
         *   pagination on the next request from where we reached our `limit` here.
         *     - We do this by setting our pageOffset to where we are now in the page, so that we
         *       can return a cursor pointing to the middle of the page (see `UserCursor`).
         *     - Then we break out of the fetch loop, because in this case, we don't want to do any
         *       of the things we normally do below:
         *         - We don't want to add to any more to the users list
         *         - We don't want to update the Cognito pagination token (since we need to fetch
         *           this same page again on the next request)
         *         - We don't want to reset the page offset
         */
        if (users.size == limit) {
          val hasMatchInRestOfPage =
              cognitoResponse.users().asSequence().drop(index).any { cognitoUser ->
                val userRole = findRolesForUser(cognitoUser, roles) ?: return@any false
                return@any filter.matches(userRole)
              }

          if (hasMatchInRestOfPage) {
            pageOffset = index
            break@fetchLoop
          } else {
            break@userLoop
          }
        }

        users.add(UserDataWithRoles.fromCognitoAndUserRole(cognitoUser, userRole))
      }

      /**
       * Advance to the next pagination token returned in the response from Cognito. It may be null,
       * if this was the last page - in that case, we break out of the loop in the `while` clause
       * below.
       *
       * It's important that we do this here and not further up, since we don't always want to
       * advance the pagination token (see comment on `users.size == limit` above).
       */
      cognitoPaginationToken = cognitoResponse.paginationToken()
      /** Reset page offset, so we start at beginning of next page. */
      pageOffset = 0
    } while (
        /**
         * We keep fetching pages while:
         * - The pagination token returned by Cognito is non-null, i.e. there are more pages to
         *   fetch
         * - The number of users fetched from Cognito for the previous page is equal to the limit
         *   (if Cognito returned less than the limit, then this was the last page)
         * - The number of filtered users is less than the limit
         */
        cognitoPaginationToken != null && fetchedFromCognito == limit && users.size < limit
    )

    return UserList(
        users = users,
        nextCursor =
            cognitoPaginationToken?.let {
              UserCursor(cognitoPaginationToken = it, pageOffset = pageOffset)
            },
    )
  }

  /** @throws PublicException To provide more context to the client about exactly what failed. */
  fun deleteUser(username: String) {
    val (cognitoClient, userPoolId) = cognitoClientWrapper.getOrThrow()

    try {
      cognitoClient.adminDeleteUser { it.userPoolId(userPoolId).username(username) }
    } catch (e: Exception) {
      throw PublicException(
          ErrorCode.INTERNAL_SERVER_ERROR,
          publicMessage = "Failed to delete user data in our identity provider (Cognito)",
          cause = e,
      )
    }

    userRoleRepo.transactional {
      val userRole = userRoleRepo.getByUserId(username)
      if (userRole == null) {
        /**
         * We expect the user to have an associated user role. If they don't, we log an error, but
         * the request can still succeed.
         */
        log.error {
          field("username", username)
          "Successfully deleted user data in Cognito, but failed to find associated user roles for that user"
        }
        return
      }

      try {
        userRoleRepo.delete(userRole)
      } catch (e: Exception) {
        throw PublicException(
            ErrorCode.INTERNAL_SERVER_ERROR,
            publicMessage =
                "Successfully deleted user data in our identity provider, but failed to delete the roles associated with the user",
            publicDetail = "Manual cleanup by a system administrator may be required",
            cause = e,
        )
      }
    }
  }
}

private fun findRolesForUser(
    cognitoUser: UserType,
    userRoles: List<Versioned<UserRole>>,
): UserRole? {
  return userRoles.find { it.data.userId == cognitoUser.username() }?.data
      ?: run {
        log.error {
          field("username", cognitoUser.username())
          "Found Cognito user without associated user roles"
        }
        return null
      }
}

data class UserList(
    val users: List<UserDataWithRoles>,
    /** See [UserCursor]. Null if there are no more users to fetch. */
    val nextCursor: UserCursor?,
)
