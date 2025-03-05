package no.liflig.userroles.common.http4k

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.http4k.core.Credentials
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.lens.basicAuthentication
import org.junit.jupiter.api.Test

class BasicAuthFilterTest {
  private val credentials = Credentials("test-user", "test-password")
  private val credentials2 = Credentials("test-user-2", "test-password-2")

  @Test
  fun testAuthMissingHeader() {
    val handler = BasicAuthFilter(credentials).then { Response(Status.OK) }

    val req = Request(Method.GET, "/")
    handler(req).status shouldBe Status.UNAUTHORIZED
  }

  @Test
  fun testAuthIncorrectHeader() {
    val handler = BasicAuthFilter(credentials).then { Response(Status.OK) }

    val req = Request(Method.GET, "/").header("Authorization", "Basic 45438583458")
    handler(req).status shouldBe Status.UNAUTHORIZED
  }

  @Test
  fun correctHeader() {
    val handler = BasicAuthFilter(credentials).then { Response(Status.OK) }

    val req = Request(Method.GET, "/").basicAuthentication(credentials)
    handler(req).status shouldNotBe Status.UNAUTHORIZED
  }

  @Test
  fun supportMultipleHeaders() {
    val handler = BasicAuthFilter(credentials, credentials2).then { Response(Status.OK) }

    val req = Request(Method.GET, "/").basicAuthentication(credentials2)
    handler(req).status shouldNotBe Status.UNAUTHORIZED
  }
}
