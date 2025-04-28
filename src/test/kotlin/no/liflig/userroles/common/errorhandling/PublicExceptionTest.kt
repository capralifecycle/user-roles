package no.liflig.userroles.common.errorhandling

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class PublicExceptionTest {
  @Test
  fun `PublicException formats exception message as expected`() {
    val cause = Exception("test-cause")

    val exception =
        PublicException(
            ErrorStatusCode.INTERNAL_SERVER_ERROR,
            publicMessage = "Something went wrong",
            publicDetail = "Terribly wrong",
            internalDetail = "Caused by failure",
            cause = cause,
        )

    exception.message shouldBe "Something went wrong (Terribly wrong) (Caused by failure)"
    exception.cause shouldBe cause
  }
}
