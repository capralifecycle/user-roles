package no.liflig.userroles.common.errorhandling

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import java.io.InputStream
import java.nio.ByteBuffer
import no.liflig.logging.field
import org.http4k.core.Body
import org.http4k.core.Response
import org.http4k.core.Status
import org.junit.jupiter.api.Test

class PublicExceptionTest {
  @Test
  fun `PublicException formats exception message as expected`() {
    val cause = Exception("test-cause")

    val exception =
        PublicException(
            "Something went wrong",
            ErrorType.INTERNAL_ERROR,
            publicDetail = "Terribly wrong",
            internalDetail = "Caused by failure",
            cause = cause,
        )

    exception.message shouldBe "Something went wrong (Terribly wrong) [Caused by failure]"
    exception.cause shouldBe cause
  }

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
      it.type shouldBe ErrorType.BAD_REQUEST
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
      it.type shouldBe ErrorType.INTERNAL_ERROR
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
}
