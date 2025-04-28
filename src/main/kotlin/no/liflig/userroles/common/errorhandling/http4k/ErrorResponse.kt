package no.liflig.userroles.common.errorhandling.http4k

import no.liflig.http4k.setup.errorResponse
import no.liflig.http4k.setup.errorhandling.ErrorResponseBody
import no.liflig.logging.field
import no.liflig.logging.rawJsonField
import no.liflig.userroles.common.errorhandling.ErrorStatusCode
import no.liflig.userroles.common.errorhandling.PublicException
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status

/**
 * Maps the [PublicException] to an HTTP response, using [PublicException.statusCode] as the status
 * code.
 *
 * The response body is JSON, using the
 * [Problem Details](https://datatracker.ietf.org/doc/html/rfc7807) specification, with:
 * - The `title` field set to [PublicException.publicMessage]
 * - The `detail` field set to [PublicException.publicDetail]
 *
 * If using the `LoggingFilter` from `liflig-http4k-setup`, the exception will also be logged, along
 * with [PublicException.internalDetail] and [PublicException.logFields].
 */
fun PublicException.toErrorResponse(request: Request): Response {
  return errorResponse(
      request,
      status = Status.fromCode(this.statusCode.httpStatusCode) ?: Status.INTERNAL_SERVER_ERROR,
      title = this.publicMessage,
      detail = this.publicDetail,
      cause = this,
      severity = this.severity,
  )
}

/**
 * Constructs a [PublicException] from the given http4k [Response]. Assumes that the response is
 * already known to be an error response, i.e. `response.status.successful` is `false`.
 *
 * The function tries to parse the response body as an [ErrorResponseBody] following the
 * [Problem Details](https://datatracker.ietf.org/doc/html/rfc7807) specification.
 * - If parsing succeeds, the [ErrorResponseBody] is translated one-to-one to a [PublicException],
 *   so the error response can be forwarded.
 * - If parsing fails, we assume that the response body may not be safe to expose. Instead, we
 *   return a [PublicException] with "Internal server error" as the [PublicException.publicMessage],
 *   and the response body included in [PublicException.logFields].
 *
 * @param source The API service where the response originated from (e.g. `"User Service"`).
 *   Included in [PublicException.internalDetail] to make debugging easier.
 */
fun PublicException.Companion.fromErrorResponse(
    response: Response,
    source: String
): PublicException {
  // We don't want to throw an exception from within this function, so we wrap the whole thing
  // in a try/catch and return a more generic PublicException if we fail.
  try {
    val errorBody = ErrorResponseBody.bodyLens(response)

    return PublicException(
        ErrorStatusCode.fromHttpStatus(errorBody.status) ?: ErrorStatusCode.INTERNAL_SERVER_ERROR,
        publicMessage = errorBody.title,
        publicDetail = errorBody.detail,
        internalDetail = "${response.status} response from ${source} - ${errorBody.instance}",
    )
  } catch (_: Exception) {
    // If we fail to parse the response body as an ErrorResponseBody, we still want to include
    // the response body in a log field for debugging. But reading the response body may fail,
    // and we don't want to throw in that case, so we wrap this in a try/catch as well.
    val responseBodyLogField =
        try {
          rawJsonField("errorResponseBody", response.bodyString(), validJson = false)
        } catch (_: Exception) {
          field("errorResponseBody", null)
        }

    return PublicException(
        ErrorStatusCode.INTERNAL_SERVER_ERROR,
        publicMessage = "Internal server error",
        internalDetail = "${response.status} response from ${source}",
        logFields = listOf(responseBodyLogField),
    )
  }
}
