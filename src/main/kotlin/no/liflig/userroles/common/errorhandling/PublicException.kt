package no.liflig.userroles.common.errorhandling

import no.liflig.logging.ExceptionWithLogFields
import no.liflig.logging.LogField
import no.liflig.logging.LogLevel
import no.liflig.logging.field
import no.liflig.logging.rawJsonField
import no.liflig.userroles.common.errorhandling.ErrorStatusCode.entries

/**
 * Exception with a message that is safe to expose publically to clients.
 *
 * If using this in an http4k server, remember to add
 * [PublicExceptionFilter][no.liflig.userroles.common.errorhandling.http4k.PublicExceptionFilter] to
 * the filter stack! Once that is done, any [PublicException]s thrown in API endpoints will be
 * mapped to appropriate HTTP responses (see
 * [toErrorResponse][no.liflig.userroles.common.errorhandling.http4k.toErrorResponse]), and also be
 * logged as part of the request log.
 *
 * If mapping a different exception to a [PublicException], remember to set [cause] so that the
 * original exception is included in the logs.
 *
 * @param statusCode The status code to use when the exception is mapped to an HTTP response.
 * @param publicMessage The safe-to-expose public message. When mapped to an HTTP response, it will
 *   be in the `title` field of the Problem Details JSON body.
 *
 *   Also included in the logged exception message.
 *
 * @param publicDetail An optional extra message to show to the client. When mapped to an HTTP
 *   response, it will be in the `detail` field of the Problem Details body.
 *
 *   Also included in the logged exception message.
 *
 * @param internalDetail Additional detail message to attach to the exception for internal logging.
 *   Will be appended in square brackets after [publicMessage]. Not included in HTTP responses.
 * @param cause If mapping a different exception to a [PublicException], set or override this
 *   parameter so that the original exception is also included in the logs.
 * @param severity By default, our `LoggingFilter` from `liflig-http4k-setup` logs at the `ERROR`
 *   log level for [ErrorStatusCode.INTERNAL_SERVER_ERROR], and `INFO` for everything else. If you
 *   want to override this log level (independent of the error status code), you can pass a custom
 *   severity for the exception here.
 * @param logFields Structured key-value fields to include when the exception is logged. You can
 *   construct fields with the [field]/[rawJsonField] functions from `liflig-logging`.
 */
open class PublicException(
    val statusCode: ErrorStatusCode,
    val publicMessage: String,
    val publicDetail: String? = null,
    val internalDetail: String? = null,
    override val cause: Throwable? = null,
    val severity: LogLevel? = null,
    logFields: List<LogField> = emptyList(),
) : ExceptionWithLogFields(logFields) {
  /**
   * This message should _not_ be exposed to clients, since it may include [internalDetail]. Use
   * [publicMessage] and [publicDetail] instead, or let the exception get caught by
   * [no.liflig.userroles.common.errorhandling.http4k.PublicExceptionFilter] to map it to an
   * appropriate HTTP response.
   */
  override val message: String = buildExceptionMessage(publicMessage, publicDetail, internalDetail)

  companion object {
    private fun buildExceptionMessage(
        publicMessage: String,
        publicDetail: String?,
        internalDetail: String?
    ): String {
      // Set capacity for efficient pre-allocation
      val capacity =
          publicMessage.length +
              (if (publicDetail == null) 0 else publicDetail.length + 3) + // +3 for " ()"
              (if (internalDetail == null) 0 else internalDetail.length + 3) // +3 for " ()"

      val message = StringBuilder(capacity)
      message.append(publicMessage)
      if (publicDetail != null) {
        message.append(" (")
        message.append(publicDetail)
        message.append(")")
      }
      if (internalDetail != null) {
        message.append(" (")
        message.append(internalDetail)
        message.append(")")
      }
      return message.toString()
    }
  }
}

/**
 * The status code to use when a [PublicException] is mapped to an HTTP response (e.g. when caught
 * by our
 * [PublicExceptionFilter][no.liflig.userroles.common.errorhandling.http4k.PublicExceptionFilter]).
 *
 * We use our own enum here instead of the `Status` class from http4k, as we don't want to depend on
 * http4k-specific things in [PublicException] (which we could adapt to other frameworks/protocols).
 */
enum class ErrorStatusCode(internal val httpStatusCode: Int) {
  /** Maps to a 400 Bad Request HTTP status. */
  BAD_REQUEST(400),
  /** Maps to a 401 Unauthorized HTTP status. */
  UNAUTHORIZED(401),
  /** Maps to a 403 Forbidden HTTP status. */
  FORBIDDEN(403),
  /** Maps to a 404 Not Found HTTP status. */
  NOT_FOUND(404),
  /** Maps to a 409 Conflict HTTP status. */
  CONFLICT(409),
  /**
   * Maps to a 500 Internal Server Error HTTP status.
   *
   * It may seem counter-intuitive to have an [INTERNAL_SERVER_ERROR] on a [PublicException], but
   * sometimes we do want to map an exception to an Internal Server Error and still provide a
   * descriptive error message to the user.
   */
  INTERNAL_SERVER_ERROR(500);

  internal companion object {
    /** Returns `null` if no [ErrorStatusCode] entry was found for the given HTTP status code. */
    internal fun fromHttpStatus(statusCode: Int): ErrorStatusCode? {
      return entries.find { it.httpStatusCode == statusCode }
    }
  }
}
