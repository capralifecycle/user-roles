package no.liflig.userroles.http4k

import no.liflig.userroles.BasicAuth
import no.liflig.userroles.common.config.http4k.createAuthFilter
import org.eclipse.jetty.http.HttpHeader
import org.eclipse.jetty.http.HttpStatus
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

  private val basicAuth = BasicAuth("testbruker", "testpassord")

  @Test
  fun testAuthMissingHeader() {
    val handler = "/arbitrary-route" bind createAuthFilter(basicAuth).then(static())

    val req = Request(Method.GET, Uri.of("/arbitrary-route"))
    assertEquals(HttpStatus.UNAUTHORIZED_401, handler(req).status.code)
  }

  @Test
  fun testAuthIncorrectHeader() {
    val handler = "/arbitrary-route" bind createAuthFilter(basicAuth).then(static())

    val req =
        Request(Method.GET, Uri.of("/arbitrary-route"))
            .header(HttpHeader.AUTHORIZATION.name, "Basic 45438583458")
    assertEquals(HttpStatus.UNAUTHORIZED_401, handler(req).status.code)
  }

  @Test
  fun correctHeader() {
    val handler = "/arbitrary-route" bind createAuthFilter(basicAuth).then(static())

    val req =
        Request(Method.GET, Uri.of("/arbitrary-route"))
            .header(HttpHeader.AUTHORIZATION.name, basicAuth.header())
    assertNotEquals(HttpStatus.UNAUTHORIZED_401, handler(req).status.code)
  }
}
