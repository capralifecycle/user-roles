package no.liflig.userroles

import mu.KotlinLogging
import no.liflig.http4k.setup.LifligBasicApiSetup
import no.liflig.http4k.setup.errorhandling.ContractLensErrorResponseRenderer
import no.liflig.http4k.setup.logging.LoggingFilter
import no.liflig.userroles.common.Api
import no.liflig.userroles.common.http4k.AuthFilter
import no.liflig.userroles.common.http4k.CustomJacksonConfig
import no.liflig.userroles.common.http4k.serverConfig
import no.liflig.userroles.features.health.HealthEndpoint
import no.liflig.userroles.features.userroles.api.UserRoleApi
import org.http4k.contract.ContractRenderer
import org.http4k.contract.PreFlightExtraction
import org.http4k.contract.bind
import org.http4k.contract.contract
import org.http4k.contract.jsonschema.v3.AutoJsonToJsonSchema
import org.http4k.contract.openapi.ApiInfo
import org.http4k.contract.openapi.ApiRenderer
import org.http4k.contract.openapi.v3.OpenApi3
import org.http4k.contract.ui.swaggerUiLite
import org.http4k.core.Method
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.format.Jackson
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Jetty
import org.http4k.server.asServer

private val log = KotlinLogging.logger {}

/** Wires up API for this service. */
class ApiServer(
    private val config: Config,
    registry: ServiceRegistry,
) {
  private val apis: List<Api> =
      listOf(
          UserRoleApi(registry.userRoleRepo),
      )

  fun start() {
    val (coreFilters, errorResponseRenderer) =
        LifligBasicApiSetup(
                logHandler = LoggingFilter.createLogHandler(),
                corsPolicy = config.api.corsPolicy,
                logHttpBody = config.api.logHttpBody,
            )
            .create(principalLog = { _ -> null })

    val contractApi = contract {
      renderer = createOpenApiRenderer(errorResponseRenderer)
      descriptionPath = "/docs/openapi-schema.json"

      for (api in apis) {
        routes += api.routes()
      }

      // PreFlightExtraction uses our endpoint contract metadata to validate the request, including
      // the body, _before_ hitting our handler. However, we want to return customized error
      // messages for invalid request bodies, so we don't want this pre-flight check. In addition,
      // this check is redundant when we extract the body in the handler anyway.
      preFlightExtraction = PreFlightExtraction.IgnoreBody
    }

    val httpHandler =
        coreFilters
            .then(AuthFilter(config.api.credentials))
            .then(
                routes(
                    "/api".bind(contractApi),
                    "/health"
                        .bind(Method.GET)
                        .to(HealthEndpoint(config.api.applicationName, config.buildInfo)),
                    swaggerUiLite { url = "/api/docs/openapi-schema.json" },
                ),
            )

    val port = config.api.serverPort
    val server = httpHandler.asServer(Jetty(port, serverConfig(port)))
    server.start()
    log.info { "Server started on port ${port}" }
  }

  private fun createOpenApiRenderer(
      errorResponseRenderer: ContractLensErrorResponseRenderer
  ): ContractRenderer {
    return OpenApi3(
        apiInfo = ApiInfo(title = config.api.applicationName, version = "v1.0"),
        servers = listOf(org.http4k.contract.openapi.v3.ApiServer(url = Uri.of("/"))),
        json = Jackson,
        apiRenderer =
            ApiRenderer.Auto(
                json = CustomJacksonConfig,
                schema = AutoJsonToJsonSchema(CustomJacksonConfig),
            ),
        errorResponseRenderer = errorResponseRenderer,
    )
  }
}
