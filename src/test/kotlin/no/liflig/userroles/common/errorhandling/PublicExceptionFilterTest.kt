package no.liflig.userroles.common.errorhandling

import no.liflig.http4k.setup.contexts
import no.liflig.http4k.setup.errorhandling.ErrorResponseBody
import org.assertj.core.api.Assertions.assertThat
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.filter.ServerFilters
import org.junit.jupiter.api.Test

class PublicExceptionFilterTest {
  @Test
  fun `filter catches and maps PublicException`() {
    val handler =
        ServerFilters.InitialiseRequestContext(contexts).then(PublicExceptionFilter()).then {
          throw PublicException(
              "Test message",
              ErrorType.BAD_REQUEST,
              publicDetail = "Test detail",
          )
        }

    val response = handler(Request(Method.GET, "/api/test"))
    assertThat(response.status).isEqualTo(Status.BAD_REQUEST)

    val responseBody = ErrorResponseBody.bodyLens(response)
    assertThat(responseBody.title).isEqualTo("Test message")
    assertThat(responseBody.detail).isEqualTo("Test detail")
    assertThat(responseBody.status).isEqualTo(400)
    assertThat(responseBody.instance).isEqualTo("/api/test")
  }
}
