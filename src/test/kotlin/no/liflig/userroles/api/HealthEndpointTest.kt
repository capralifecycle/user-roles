package no.liflig.userroles.api

import io.kotest.matchers.shouldBe
import no.liflig.userroles.testutils.TestServices
import org.http4k.client.JavaHttpClient
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class HealthEndpointTest {
  @RegisterExtension private val services = TestServices.get()

  /**
   * We set up our own HTTP client here instead of using [TestServices.apiClient], since we want to
   * test with a real HTTP request here to ensure that our HTTP server setup works as expected.
   */
  private val httpClient = JavaHttpClient()

  @Test
  fun `health should respond 200 OK`() {
    val response = httpClient(Request(Method.GET, "${services.baseUrl}/health"))
    response.status shouldBe Status.OK
  }
}
