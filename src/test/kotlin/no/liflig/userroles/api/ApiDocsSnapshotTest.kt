package no.liflig.userroles.api

import io.kotest.matchers.shouldBe
import no.liflig.snapshot.verifyJsonSnapshot
import no.liflig.userroles.testutils.FlowTestExtension
import no.liflig.userroles.testutils.TestServices
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(FlowTestExtension::class)
class ApiDocsSnapshotTest {
  @Test
  fun `exposes expected open-api-schema in JSON format`(services: TestServices) {
    val response = services.sendOpenApiSchemaRequest()
    response.status shouldBe Status.OK
    verifyJsonSnapshot("openapi-schema.json", response.bodyString())
  }
}

private fun TestServices.sendOpenApiSchemaRequest(): Response {
  return httpClient(
      Request(Method.GET, "${baseUrl}/api/docs/openapi-schema.json"),
  )
}
