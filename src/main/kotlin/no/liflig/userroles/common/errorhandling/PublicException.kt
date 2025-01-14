package no.liflig.userroles.common.errorhandling

import no.liflig.http4k.setup.errorResponse
import no.liflig.http4k.setup.errorhandling.ErrorResponseBody
import no.liflig.logging.ExceptionWithLogFields
import no.liflig.logging.LogField
import no.liflig.logging.field
import no.liflig.logging.rawJsonField
import no.liflig.userroles.common.errorhandling.ErrorType.entries
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status

/**
 * Exception with a message that is safe to expose publically to clients.
 *
 * If using this in an http4k server, remember to add [PublicExceptionFilter] to the filter stack!
 * Once that is done, any [PublicException]s thrown in API endpoints will be mapped to appropriate
 * HTTP responses (see [toErrorResponse]), and also be logged as part of the request log.
 *
 * If mapping a different exception to a [PublicException], remember to set [cause] so that the the
 * original exception is included in the logs!
 *
 * @param publicMessage The safe-to-expose public message. When mapped to an HTTP response, it will
 *   be in the "title" field of the Problem Details body.
 *
 *   Also included in the logged exception message.
 *
 * @param type Generic error type, so that service/repository layers do not have to import
 *   HTTP-specific things when defining a [PublicException].
 *
 *   Maps to a corresponding HTTP status code if the exception is transformed to an HTTP response.
 *
 * @param publicDetail An optional extra message to show to the client. When mapped to an HTTP
 *   response, it will be in the "detail" field of the Problem Details body.
 *
 *   Also included in the logged exception message.
 *
 * @param internalDetail Additional detail message to attach to the exception for internal logging.
 *   Will be appended in square brackets after [publicMessage]. Not included in HTTP responses.
 * @param cause If mapping a different exception to a [PublicException], set or override this
 *   parameter so that the original exception is also included in the logs.
 * @param logFields Structured key-value fields to include when the exception is logged. You can
 *   construct fields with the [field]/[rawJsonField] functions from `liflig-logging`.
 */
open class PublicException(
    val publicMessage: String,
    val type: ErrorType,
    val publicDetail: String? = null,
    val internalDetail: String? = null,
    override val cause: Exception? = null,
    logFields: List<LogField> = emptyList(),
) : ExceptionWithLogFields(logFields) {
  /**
   * This message should _not_ be exposed to clients, since it may include [internalDetail]. Use
   * [publicMessage] and [publicDetail] instead, or let the exception get caught by
   * [PublicExceptionFilter] to map it to an appropriate HTTP response.
   */
  override val message: String = buildExceptionMessage(publicMessage, publicDetail, internalDetail)

  /**
   * Maps the [PublicException] to an HTTP response with the [publicMessage], [publicDetail] and
   * [type] (mapped to an HTTP status code) in a
   * [Problem Details](https://datatracker.ietf.org/doc/html/rfc7807) JSON response body. The
   * exception will also be logged, along with any given [internalDetail].
   */
  fun toErrorResponse(request: Request): Response {
    return errorResponse(
        request,
        status = type.httpStatus,
        title = publicMessage,
        detail = publicDetail,
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
     *   response body included in [internalDetail].
     *
     * @param source The API service where the response originated from (e.g. `"User Service"`).
     *   Included in [internalDetail] of the [PublicException] to make debugging easier.
     */
    fun fromErrorResponse(response: Response, source: String): PublicException {
      // We don't want to throw an exception from within this function, so we wrap the whole thing
      // in a try/catch and return a more generic PublicException if we fail.
      try {
        val errorBody = ErrorResponseBody.bodyLens(response)

        return PublicException(
            publicMessage = errorBody.title,
            publicDetail = errorBody.detail,
            type = ErrorType.fromHttpStatus(errorBody.status) ?: ErrorType.INTERNAL_ERROR,
            internalDetail = "${response.status} response from ${source} - ${errorBody.instance}",
        )
      } catch (_: Exception) {
        // If we fail to parse the response body as an ErrorResponseBody, we still want to include
        // the response body in a log field for debugging. But reading the response body may fail,
        // and we don't want to throw in that case, so we wrap this in a try/catch as well.
        val responseBodyLogField =
            try {
              rawJsonField("errorResponseBody", response.bodyString(), validJson = false)
            } catch (e: Exception) {
              field("errorResponseBody", null)
            }

        return PublicException(
            "Internal server error",
            type = ErrorType.INTERNAL_ERROR,
            internalDetail = "${response.status} response from ${source}",
            logFields = listOf(responseBodyLogField),
        )
      }
    }

    private fun buildExceptionMessage(
        publicMessage: String,
        publicDetail: String?,
        internalDetail: String?
    ): String {
      // Set capacity for efficient pre-allocation
      val capacity =
          publicMessage.length +
              (if (publicDetail == null) 0 else publicDetail.length + 3) + // +3 for " ()"
              (if (internalDetail == null) 0 else internalDetail.length + 3) // +3 for " []"

      val message = StringBuilder(capacity)
      message.append(publicMessage)
      if (publicDetail != null) {
        message.append(" (")
        message.append(publicDetail)
        message.append(")")
      }
      if (internalDetail != null) {
        message.append(" [")
        message.append(internalDetail)
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
 * When a [PublicException] is thrown in the context of an HTTP request, the [PublicExceptionFilter]
 * will catch it and map it to an appropriate HTTP response, with the [httpStatus] here.
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
