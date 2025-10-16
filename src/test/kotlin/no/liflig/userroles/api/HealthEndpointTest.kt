package no.liflig.userroles.api

import io.kotest.matchers.shouldBe
import no.liflig.userroles.testutils.TestServices
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class HealthEndpointTest {
  @RegisterExtension private val services = TestServices.get()

  @Test
  fun `health should respond 200 OK`() {
    val response = services.httpClient(Request(Method.GET, "${services.baseUrl}/health"))
    response.status shouldBe Status.OK
  }
}
