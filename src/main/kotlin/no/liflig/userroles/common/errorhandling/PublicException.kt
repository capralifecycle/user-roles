package no.liflig.userroles.common.errorhandling

import no.liflig.http4k.setup.errorResponse
import no.liflig.http4k.setup.errorhandling.ErrorResponseBody
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status

/**
 * Exception with a message that is safe to expose publically to clients.
 *
 * If using this in an http4k server, remember to add the [MapPublicExceptionsToErrorResponseFilter]
 * to the API. Once that is done, the filter will catch any thrown [PublicException]s and map them
 * to appropriate HTTP responses (see [toErrorResponse]), and also log the exception as part of the
 * request log.
 *
 * If mapping a different exception to a [PublicException], remember to set [cause] so that the
 * details of the original exception are included in the logs!
 */
open class PublicException(
    /**
     * The safe-to-expose public message. When mapped to an HTTP response, it will be in the "title"
     * field of the Problem Details body.
     *
     * Also included in the logged exception message.
     */
    val publicMessage: String,
    /**
     * An optional extra message with more details to show to the client. When mapped to an HTTP
     * response, it will be in the "detail" field of the Problem Details body.
     *
     * Also included in the logged exception message.
     */
    val publicDetails: String? = null,
    /**
     * Generic error type, so that service/repository layers do not have to import HTTP-specific
     * things when defining a [PublicException].
     *
     * Maps to a corresponding HTTP status code if the exception is transformed to an HTTP response.
     */
    val type: ErrorType = ErrorType.INTERNAL_ERROR,
    /**
     * Additional details to attach to the exception message for internal logging. Will be appended
     * in square brackets after [publicMessage].
     */
    val internalDetails: String? = null,
    /**
     * If mapping a different exception to a [PublicException], set or override this parameter so
     * that the details of the original exception are included in the logs.
     */
    override val cause: Exception? = null,
) : RuntimeException() {
  /**
   * This message should _not_ be exposed to clients, since it may include [internalDetails]. Use
   * [publicMessage] and [publicDetails] instead, or let the exception get caught by
   * [MapPublicExceptionsToErrorResponseFilter] to map it to an appropriate HTTP response.
   */
  override val message: String =
      buildExceptionMessage(publicMessage, publicDetails, internalDetails)

  /**
   * Maps the [PublicException] to an HTTP response with the [publicMessage], [publicDetails] and
   * [type] (mapped to an HTTP status code) in a
   * [Problem Details](https://datatracker.ietf.org/doc/html/rfc7807) JSON response body. The
   * exception will also be logged, along with any given [internalDetails].
   */
  fun toErrorResponse(request: Request): Response {
    return errorResponse(
        request,
        status = type.httpStatus,
        title = publicMessage,
        detail = publicDetails,
        cause = this,
    )
  }

  companion object {
    /**
     * Constructs a [PublicException] from the given http4k [Response]. Assumes that the response is
     * already known to be an error response, i.e. `response.status.successful` is `false`.
     *
     * The function tries to parse the response body as an [ErrorResponseBody] following the
     * [Problem Details](https://datatracker.ietf.org/doc/html/rfc7807) specification.
     * - If parsing succeeds, the [ErrorResponseBody] is translated one-to-one to a
     *   [PublicException], so the error response can be forwarded.
     * - If parsing fails, we assume that the response body may not be safe to expose. Instead, we
     *   return a [PublicException] with "Internal server error" as the [publicMessage], and the
     *   response body included in [internalDetails].
     *
     * @param source The API service where the response originated from (e.g. `"User Service"`).
     *   Included in [internalDetails] of the [PublicException] to make debugging easier.
     */
    fun fromErrorResponse(response: Response, source: String): PublicException {
      // We don't want to throw an exception from within this function, so we wrap the whole thing
      // in a try/catch and return a more generic PublicException if we fail.
      try {
        val errorBody = ErrorResponseBody.bodyLens(response)

        return PublicException(
            publicMessage = errorBody.title,
            publicDetails = errorBody.detail,
            type = ErrorType.fromHttpStatus(errorBody.status) ?: ErrorType.INTERNAL_ERROR,
            internalDetails = "${response.status} response from ${source} - ${errorBody.instance}",
        )
      } catch (_: Exception) {
        var internalDetails = "${response.status} response from ${source}"

        // If we fail to parse the response body as an ErrorResponseBody, we still want to include
        // the response body in internalDetails for debugging. But reading the response body may
        // fail, and we don't want to throw in that case, so we wrap this in a try/catch as well.
        internalDetails +=
            try {
              val responseBody = response.bodyString()
              if (responseBody == "") {
                ", with empty body"
              } else {
                ", with body: ${responseBody}"
              }
            } catch (e: Exception) {
              ", and failed to read body: ${e.message}"
            }

        return PublicException(
            "Internal server error",
            type = ErrorType.INTERNAL_ERROR,
            internalDetails = internalDetails,
        )
      }
    }

    private fun buildExceptionMessage(
        publicMessage: String,
        publicDetails: String?,
        internalDetails: String?
    ): String {
      // Set capacity for efficient pre-allocation
      val capacity =
          publicMessage.length +
              (if (publicDetails == null) 0 else publicDetails.length + 3) + // +3 for " ()"
              (if (internalDetails == null) 0 else internalDetails.length + 3) // +3 for " []"

      val message = StringBuilder(capacity)
      message.append(publicMessage)
      if (publicDetails != null) {
        message.append(" (")
        message.append(publicDetails)
        message.append(")")
      }
      if (internalDetails != null) {
        message.append(" [")
        message.append(internalDetails)
        message.append("]")
      }
      return message.toString()
    }
  }
}

/**
 * Generic error type, so that service/repository layers do not have to import HTTP-specific things
 * when defining a [PublicException].
 *
 * When a [PublicException] is thrown in the context of an HTTP request, the
 * [MapPublicExceptionsToErrorResponseFilter] will catch it and map it to an appropriate HTTP
 * response, with the [httpStatus] here.
 */
@Suppress("unused")
enum class ErrorType(val httpStatus: Status) {
  BAD_REQUEST(httpStatus = Status.BAD_REQUEST),
  UNAUTHORIZED(httpStatus = Status.UNAUTHORIZED),
  FORBIDDEN(httpStatus = Status.FORBIDDEN),
  NOT_FOUND(httpStatus = Status.NOT_FOUND),
  CONFLICT(httpStatus = Status.CONFLICT),
  /**
   * It may seem counter-intuitive to have an [INTERNAL_ERROR] on a [PublicException], but sometimes
   * we do want to map an exception to a 500 Internal Server Error and still provide a descriptive
   * error message to the user. Also, 500 errors are logged at a higher severity than 4XX errors in
   * our http4k logging filter, so critical errors should use this.
   */
  INTERNAL_ERROR(httpStatus = Status.INTERNAL_SERVER_ERROR);

  internal companion object {
    internal fun fromHttpStatus(statusCode: Int): ErrorType? {
      return entries.find { it.httpStatus.code == statusCode }
    }
  }
}
