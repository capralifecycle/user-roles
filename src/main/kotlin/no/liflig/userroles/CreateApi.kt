package no.liflig.userroles

import no.liflig.http4k.setup.LifligBasicApiSetup
import no.liflig.http4k.setup.logging.LoggingFilter
import no.liflig.userroles.common.config.http4k.JacksonJson
import no.liflig.userroles.common.config.http4k.createAuthFilter
import no.liflig.userroles.features.health.healthEndpoint
import no.liflig.userroles.features.userroles.api.UserRoleApi
import org.http4k.contract.PreFlightExtraction
import org.http4k.contract.bind
import org.http4k.contract.contract
import org.http4k.contract.jsonschema.v3.AutoJsonToJsonSchema
import org.http4k.contract.openapi.ApiInfo
import org.http4k.contract.openapi.ApiRenderer
import org.http4k.contract.openapi.v3.ApiServer
import org.http4k.contract.openapi.v3.OpenApi3
import org.http4k.contract.ui.swaggerUiLite
import org.http4k.core.Method
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.format.Jackson
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes

/** Wires up API for this service. */
fun createApi(
    config: Config,
    registry: ServiceRegistry,
): RoutingHttpHandler {
  val (coreFilters, errorResponseRenderer) =
      LifligBasicApiSetup(
              logHandler = LoggingFilter.createLogHandler(),
              corsPolicy = config.corsPolicy,
              logHttpBody = config.logRequestBody,
          )
          .create(principalLog = { _ -> null })

  val api = contract {
    renderer =
        OpenApi3(
            apiInfo = ApiInfo("User Roles API", "v1.0"),
            servers = listOf(ApiServer(url = Uri.of("/"))),
            json = Jackson,
            apiRenderer =
                ApiRenderer.Auto(
                    json = JacksonJson,
                    schema = AutoJsonToJsonSchema(JacksonJson),
                ),
            errorResponseRenderer = errorResponseRenderer,
        )
    descriptionPath = "/docs/openapi-schema.json"

    routes += UserRoleApi(registry).getRoutes()

    // PreFlightExtraction uses our endpoint contract metadata to validate the request, including
    // the body, _before_ hitting our handler. However, we want to return customized error messages
    // for invalid request bodies, so we don't want this pre-flight check. In addition, this check
    // does not work with our LogUploadEndpoint, where we accept both gzipped and non-gzipped
    // bodies.
    preFlightExtraction = PreFlightExtraction.IgnoreBody
  }

  return coreFilters
      .then(createAuthFilter(config.basicAuth))
      .then(
          routes(
              "/api" bind api,
              "/health" bind Method.GET to healthEndpoint(registry.healthService),
              swaggerUiLite { url = "/api/docs/openapi-schema.json" },
          ),
      )
}
