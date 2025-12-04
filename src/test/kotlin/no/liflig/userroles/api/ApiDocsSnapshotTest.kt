package no.liflig.userroles.api

import io.kotest.matchers.shouldBe
import no.liflig.snapshot.verifyJsonSnapshot
import no.liflig.userroles.testutils.TestServices
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class ApiDocsSnapshotTest {
  @RegisterExtension private val services = TestServices.get()

  @Test
  fun `exposes expected open-api-schema in JSON format`() {
    val response = services.sendOpenApiSchemaRequest()
    response.status shouldBe Status.OK
    verifyJsonSnapshot("openapi-schema.json", response.bodyString())
  }
}

private fun TestServices.sendOpenApiSchemaRequest(): Response {
  return apiClient(
      Request(Method.GET, "${baseUrl}/api/docs/openapi-schema.json"),
  )
}
