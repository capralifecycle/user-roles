package no.liflig.userroles.common.config.http4k

import no.liflig.userroles.BasicAuth
import org.eclipse.jetty.http.HttpHeader
import org.http4k.core.Filter
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status

fun createAuthFilter(basicAuth: BasicAuth): Filter = Filter {
  { request: Request ->
    when (request.uri.path) {
      "/health" -> it(request)
      else -> {
        val authHeader = request.header(HttpHeader.AUTHORIZATION.name)
        if (authHeader != basicAuth.header()) {
          Response(Status.UNAUTHORIZED).replaceHeader("WWW-Authenticate", "Basic")
        } else {
          it(request)
        }
      }
    }
  }
}
