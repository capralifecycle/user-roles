package no.liflig.userroles.common.errorhandling

import org.http4k.core.Filter
import org.http4k.core.HttpHandler

/**
 * http4k filter that catches [PublicException]s and maps them to error responses (see
 * [PublicException.toErrorResponse]).
 *
 * ### Usage
 *
 * Call `.then()` on your existing filters to add this to the stack:
 * ```
 * filters
 *     .then(PublicExceptionFilter())
 *     .then(/* Other filters or routes */)
 * ```
 *
 * This filter should be added after more general exception-catching filters, so that
 * PublicExceptions are caught first.
 */
class PublicExceptionFilter : Filter {
  override fun invoke(nextHandler: HttpHandler): HttpHandler {
    return { request ->
      try {
        nextHandler(request)
      } catch (e: PublicException) {
        e.toErrorResponse(request)
      }
    }
  }
}
