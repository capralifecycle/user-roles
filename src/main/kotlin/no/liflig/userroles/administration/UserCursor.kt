package no.liflig.userroles.administration

import no.liflig.publicexception.ErrorCode
import no.liflig.publicexception.PublicException

/**
 * Cursor-based pagination for fetching the next page of users (sent in the `cursor` query param of
 * the List Users endpoint).
 *
 * We base the cursor on 2 things (concatenated to a string in order to be opaque to the client):
 * - A cursor from our identity provider
 *     - In AWS Cognito, this is the `PaginationToken` returned by the `ListUsers` API.
 * - A page offset within the last fetched page from our identity provider
 *     - We need this because we do client-side filtering of users based on their roles, and so we
 *       may need to fetch _parts_ of more pages from the identity provider, in order to return
 *       exactly the `limit` in the request to the List Users endpoint.
 */
data class UserCursor(
    val cursorFromIdentityProvider: String,
    val pageOffset: Int,
) {
  override fun toString(): String = "${cursorFromIdentityProvider}${SEPARATOR}${pageOffset}"

  companion object {
    /** @throws InvalidUserCursor */
    fun fromString(cursor: String, limit: Int): UserCursor {
      /**
       * Use substringBeforeLast/substringAfterLast, since the separator may hypothetically appear
       * in the identity provider cursor, but we know it doesn't appear in the page offset.
       */
      val cursorFromIdentityProvider =
          cursor.substringBeforeLast(SEPARATOR, missingDelimiterValue = "")

      val pageOffsetString = cursor.substringAfterLast(SEPARATOR, missingDelimiterValue = "")

      if (cursorFromIdentityProvider == "" || pageOffsetString == "") {
        throw InvalidUserCursor(
            cursor = cursor,
            publicDetail = "Expected to find separator '${SEPARATOR}' in cursor",
        )
      }

      val pageOffset =
          pageOffsetString.toIntOrNull()
              ?: throw InvalidUserCursor(
                  cursor = cursor,
                  publicDetail = "Could not parse page offset '${pageOffsetString}' as integer",
              )

      if (pageOffset !in 0..limit) {
        throw InvalidUserCursor(
            cursor = cursor,
            publicDetail =
                "Page offset in cursor must be in range [0, ${limit}], was ${pageOffset}",
        )
      }

      return UserCursor(
          cursorFromIdentityProvider = cursorFromIdentityProvider,
          pageOffset = pageOffset,
      )
    }

    /**
     * Separator chosen that should not appear in either [cursorFromIdentityProvider] or
     * [pageOffset], so we can reliably split on it.
     */
    private const val SEPARATOR = "___"
  }
}

/** Maps to a 400 Bad Request response, with the given detail message in the error response body. */
class InvalidUserCursor(cursor: String, publicDetail: String) :
    PublicException(
        ErrorCode.BAD_REQUEST,
        publicMessage = "Invalid cursor '${cursor}' in List Users request",
        publicDetail = publicDetail,
    )
