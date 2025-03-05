package no.liflig.userroles.common.errorhandling

import io.kotest.matchers.shouldBe
import no.liflig.http4k.setup.errorhandling.ErrorResponseBody
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
              "Test message",
              ErrorType.BAD_REQUEST,
              publicDetail = "Test detail",
          )
        }

    val response = handler(Request(Method.GET, "/api/test"))
    response.status shouldBe Status.BAD_REQUEST

    val responseBody = ErrorResponseBody.bodyLens(response)
    responseBody.title shouldBe "Test message"
    responseBody.detail shouldBe "Test detail"
    responseBody.status shouldBe 400
    responseBody.instance shouldBe "/api/test"
  }
}
