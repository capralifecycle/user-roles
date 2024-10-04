package no.liflig.userroles.api

import no.liflig.userroles.testutils.FlowTestExtension
import no.liflig.userroles.testutils.TestServices
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(FlowTestExtension::class)
class HealthEndpointTest {
  @Test
  fun `health should respond 200 OK`(services: TestServices) {
    val response = services.httpClient(Request(Method.GET, "${services.baseUrl}/health"))
    assertEquals(Status.OK, response.status)
  }
}
