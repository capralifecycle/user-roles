package no.liflig.userroles.administration

import no.liflig.publicexception.ErrorCode
import no.liflig.publicexception.PublicException

/**
 * Cursor-based pagination for fetching the next page of users (sent in the `cursor` query param of
 * the List Users endpoint).
 *
 * We base the cursor on 2 things (concatenated to a string in order to be opaque to the client):
 * - A Cognito pagination token
 * - A page offset within the last fetched page from Cognito
 *     - We need this because we do client-side filtering of users based on their roles, and so we
 *       may need to fetch _parts_ of more pages from Cognito, in order to return exactly the
 *       `limit` in the request to the List Users endpoint.
 */
data class UserCursor(
    val cognitoPaginationToken: String,
    val pageOffset: Int,
) {
  override fun toString(): String = "${cognitoPaginationToken}${SEPARATOR}${pageOffset}"

  companion object {
    fun fromString(cursor: String): UserCursor {
      val split = cursor.split(SEPARATOR, limit = 2)
      if (split.size != 2) {
        throw PublicException(
            ErrorCode.BAD_REQUEST,
            publicMessage = "Invalid user cursor '${cursor}': Could not split at '${SEPARATOR}'",
        )
      }

      val (cognitoPaginationToken, pageOffsetString) = split
      val pageOffset =
          pageOffsetString.toIntOrNull()
              ?: throw PublicException(
                  ErrorCode.BAD_REQUEST,
                  publicMessage =
                      "Invalid user cursor '${cursor}': Could not parse page offset '${pageOffsetString}' as integer",
              )

      return UserCursor(
          cognitoPaginationToken = cognitoPaginationToken,
          pageOffset = pageOffset,
      )
    }

    /**
     * Separator chosen that should not appear in either [cognitoPaginationToken] or [pageOffset],
     * so we can reliably split on it.
     */
    private const val SEPARATOR = "___"
  }
}
