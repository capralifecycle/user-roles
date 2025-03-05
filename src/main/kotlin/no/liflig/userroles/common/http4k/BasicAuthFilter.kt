package no.liflig.userroles.common.http4k

import java.util.Base64
import no.liflig.http4k.setup.errorResponse
import org.eclipse.jetty.http.HttpHeader
import org.http4k.core.Credentials
import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Status

/**
 * An http4k filter for verifying basic auth. Supports one or more sets of basic auth credentials.
 */
class BasicAuthFilter(vararg credentials: Credentials) : Filter {
  private val expectedAuthHeaders = credentials.map { basicAuthHeader(it) }

  override fun invoke(handler: HttpHandler): HttpHandler {
    return { request ->
      when (request.uri.path) {
        "/health" -> handler(request)
        "/api/docs/openapi-schema.json" -> handler(request)
        else -> {
          if (hasValidAuthHeader(request)) {
            handler(request)
          } else {
            errorResponse(request, Status.UNAUTHORIZED, "Unauthenticated")
                .replaceHeader("WWW-Authenticate", "Basic")
          }
        }
      }
    }
  }

  private fun hasValidAuthHeader(request: Request): Boolean {
    val authHeader = request.header(HttpHeader.AUTHORIZATION.name)

    return expectedAuthHeaders.any { expected -> authHeader == expected }
  }
}

fun basicAuthHeader(credentials: Credentials): String {
  return "Basic " +
      Base64.getEncoder()
          .encodeToString("${credentials.user}:${credentials.password}".toByteArray())
}
