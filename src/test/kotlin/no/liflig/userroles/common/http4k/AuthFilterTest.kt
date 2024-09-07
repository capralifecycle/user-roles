package no.liflig.userroles.common.http4k

import org.eclipse.jetty.http.HttpHeader
import org.eclipse.jetty.http.HttpStatus
import org.http4k.core.Credentials
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.routing.bind
import org.http4k.routing.static
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class AuthFilterTest {
  private val credentials = Credentials("tomratestbruker", "tomratestpassord")
  private val credentials2 = Credentials("tomratestbruker2", "tomratestpassord2")

  @Test
  fun testAuthMissingHeader() {
    val handler = "/arbitrary-route".bind(AuthFilter(credentials).then(static()))

    val req = Request(Method.GET, Uri.of("/arbitrary-route"))
    assertEquals(HttpStatus.UNAUTHORIZED_401, handler(req).status.code)
  }

  @Test
  fun testAuthIncorrectHeader() {
    val handler = "/arbitrary-route".bind(AuthFilter(credentials).then(static()))

    val req =
        Request(Method.GET, Uri.of("/arbitrary-route"))
            .header(HttpHeader.AUTHORIZATION.name, "Basic 45438583458")
    assertEquals(HttpStatus.UNAUTHORIZED_401, handler(req).status.code)
  }

  @Test
  fun correctHeader() {
    val handler = "/arbitrary-route".bind(AuthFilter(credentials).then(static()))

    val req =
        Request(Method.GET, Uri.of("/arbitrary-route"))
            .header(HttpHeader.AUTHORIZATION.name, basicAuthHeader(credentials))
    assertNotEquals(HttpStatus.UNAUTHORIZED_401, handler(req).status.code)
  }

  @Test
  fun supportMultipleHeaders() {
    val handler = "/arbitrary-route".bind(AuthFilter(credentials, credentials2).then(static()))

    val req =
        Request(Method.GET, Uri.of("/arbitrary-route"))
            .header(
                HttpHeader.AUTHORIZATION.name,
                basicAuthHeader(credentials2),
            )
    assertNotEquals(HttpStatus.UNAUTHORIZED_401, handler(req).status.code)
  }
}
