package no.liflig.userroles.common.errorhandling

import org.http4k.core.Filter
import org.http4k.core.HttpHandler

/**
 * Filter that catches [PublicException]s and maps them to error responses (see
 * [PublicException.toErrorResponse]).
 */
class MapPublicExceptionsToErrorResponseFilter : Filter {
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
