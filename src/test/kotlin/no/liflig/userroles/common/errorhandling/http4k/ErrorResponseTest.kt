package no.liflig.userroles.common.errorhandling.http4k

import io.kotest.assertions.asClue
import io.kotest.assertions.withClue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.io.InputStream
import java.nio.ByteBuffer
import no.liflig.logging.field
import no.liflig.userroles.common.errorhandling.ErrorStatusCode
import no.liflig.userroles.common.errorhandling.PublicException
import org.http4k.core.Body
import org.http4k.core.Response
import org.http4k.core.Status
import org.junit.jupiter.api.Test

class ErrorResponseTest {
  @Test
  fun `fromErrorResponse correctly parses a Problem Details body`() {
    val response =
        Response(Status.BAD_REQUEST)
            .body(
                """
                {"title":"test-title","detail":"test-detail","status":400,"instance":"/api/test"}
                """
                    .trimIndent(),
            )

    val exception = PublicException.fromErrorResponse(response, source = "Test Service")
    exception.asClue {
      it.publicMessage shouldBe "test-title"
      it.publicDetail shouldBe "test-detail"
      it.statusCode shouldBe ErrorStatusCode.BAD_REQUEST
      it.internalDetail shouldBe "400 Bad Request response from Test Service - /api/test"
    }
  }

  @Test
  fun `fromErrorResponse does not expose non-Problem Details body`() {
    val response = Response(Status.BAD_REQUEST).body("Something went wrong")

    val exception = PublicException.fromErrorResponse(response, source = "Test Service")
    exception.asClue {
      it.publicMessage shouldBe "Internal server error"
      it.publicDetail shouldBe null
      it.statusCode shouldBe ErrorStatusCode.INTERNAL_SERVER_ERROR
      it.internalDetail shouldBe "400 Bad Request response from Test Service"
      it.logFields shouldBe listOf(field("errorResponseBody", "Something went wrong"))
    }
  }

  @Test
  fun `fromErrorResponse indicates if body is empty for non-Problem Details response`() {
    val response = Response(Status.FORBIDDEN)

    val exception = PublicException.fromErrorResponse(response, source = "Test Service")
    exception.internalDetail shouldBe "403 Forbidden response from Test Service"
    exception.logFields shouldBe listOf(field("errorResponseBody", ""))
  }

  @Test
  fun `fromErrorResponse indicates if it failed to read body for non-Problem Details response`() {
    class AlwaysFailingBody : Body {
      override val payload: ByteBuffer
        get() = throw Exception("Body failed")

      override val length: Long? = null
      override val stream: InputStream = InputStream.nullInputStream()

      override fun close() {}
    }

    val response = Response(Status.INTERNAL_SERVER_ERROR).body(AlwaysFailingBody())

    val exception = PublicException.fromErrorResponse(response, source = "Test Service")
    exception.internalDetail shouldBe "500 Internal Server Error response from Test Service"
    exception.logFields shouldBe listOf(field("errorResponseBody", "null"))
  }

  @Test
  fun `ErrorStatusCode has valid HTTP status codes`() {
    for (errorType in ErrorStatusCode.entries) {
      withClue(errorType) {
        val status = Status.fromCode(errorType.httpStatusCode)
        status.shouldNotBeNull()
      }
    }
  }
}
