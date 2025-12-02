package no.liflig.userroles.administration

import no.liflig.logging.getLogger
import no.liflig.userroles.roles.UserRoleRepository
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient

private val log = getLogger()

class UserAdministrationService(
    private val userRoleRepository: UserRoleRepository,
    private val cognitoClient: CognitoIdentityProviderClient,
    private val userPoolId: String,
) {
  /**
   * @param limit Number of users to return. Minimum 1, max 60.
   * @param cursor Null if this is the first fetch.
   */
  fun listUsers(
      limit: Int,
      filter: UserFilter,
      cursor: UserCursor?,
  ): UsersList {
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
          cognitoClient.listUsers { request ->
            request.limit(limit).userPoolId(userPoolId)
            cognitoPaginationToken?.let { request.paginationToken(it) }
            cognitoFilterString?.let { request.filter(it) }
          }

      fetchedFromCognito = cognitoResponse.users().size
      if (fetchedFromCognito == 0) {
        break@fetchLoop
      }

      val roles =
          userRoleRepository.listByUserIds(userIds = cognitoResponse.users().map { it.username() })

      userLoop@ for ((index, cognitoUser) in cognitoResponse.users().withIndex().drop(pageOffset)) {
        val role =
            roles.find { it.data.userId == cognitoUser.username() }
                ?: run {
                  log.error {
                    field("cognitoUsername", cognitoUser.username())
                    "Found Cognito user without corresponding user role"
                  }
                  continue@userLoop
                }

        if (!filter.matches(role.data)) {
          continue@userLoop
        }

        /**
         * If we reach our `limit` halfway through, then we:
         * - Set our page offset to where we are now in the page, so that we can return a cursor
         *   pointing to the middle of the page (see [UserCursor])
         * - Break out of the fetch loop, because in this case, we don't want to do any of the
         *   things we normally do below:
         *     - We don't want to add to any more to the users list
         *     - We don't want to update the Cognito pagination token (since we need to fetch this
         *       same page again on the same request)
         *     - We don't want to reset the page offset
         */
        if (users.size == limit) {
          pageOffset = index
          break@fetchLoop
        }

        users.add(UserDataWithRoles.fromCognitoAndUserRole(cognitoUser, role.data))
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

    return UsersList(
        users = users,
        nextCursor =
            cognitoPaginationToken?.let {
              UserCursor(cognitoPaginationToken = it, pageOffset = pageOffset)
            },
    )
  }
}

data class UsersList(
    val users: List<UserDataWithRoles>,
    /** See [UserCursor]. Null if there are no more users to fetch. */
    val nextCursor: UserCursor?,
)
