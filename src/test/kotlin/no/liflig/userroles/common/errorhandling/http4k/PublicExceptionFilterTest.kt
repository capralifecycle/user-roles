package no.liflig.userroles.common.errorhandling.http4k

import io.kotest.matchers.shouldBe
import no.liflig.http4k.setup.errorhandling.ErrorResponseBody
import no.liflig.userroles.common.errorhandling.ErrorStatusCode
import no.liflig.userroles.common.errorhandling.PublicException
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.core.then
import org.junit.jupiter.api.Test

class PublicExceptionFilterTest {
  @Test
  fun `filter catches and maps PublicException`() {
    val handler =
        PublicExceptionFilter().then {
          throw PublicException(
              ErrorStatusCode.BAD_REQUEST,
              publicMessage = "Test message",
              publicDetail = "Test detail",
          )
        }

    val response = handler(Request.Companion(Method.GET, "/api/test"))
    response.status shouldBe Status.Companion.BAD_REQUEST

    val responseBody = ErrorResponseBody.bodyLens(response)
    responseBody.title shouldBe "Test message"
    responseBody.detail shouldBe "Test detail"
    responseBody.status shouldBe 400
    responseBody.instance shouldBe "/api/test"
  }
}
