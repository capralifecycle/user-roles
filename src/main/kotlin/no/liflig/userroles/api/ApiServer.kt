package no.liflig.userroles.api

import com.fasterxml.jackson.databind.JsonNode
import no.liflig.http4k.setup.LifligBasicApiSetup
import no.liflig.http4k.setup.LifligBasicApiSetupConfig
import no.liflig.http4k.setup.errorhandling.ContractLensErrorResponseRenderer
import no.liflig.http4k.setup.logging.LoggingFilter
import no.liflig.logging.getLogger
import no.liflig.userroles.App
import no.liflig.userroles.common.config.Config
import no.liflig.userroles.common.errorhandling.PublicExceptionFilter
import no.liflig.userroles.common.http4k.AuthFilter
import no.liflig.userroles.common.http4k.CustomJacksonConfig
import no.liflig.userroles.common.http4k.EndpointGroup
import no.liflig.userroles.features.userroles.api.UserRoleApi
import org.eclipse.jetty.server.HttpConnectionFactory
import org.http4k.contract.PreFlightExtraction
import org.http4k.contract.contract
import org.http4k.contract.jsonschema.v3.AutoJsonToJsonSchema
import org.http4k.contract.openapi.ApiInfo
import org.http4k.contract.openapi.ApiRenderer
import org.http4k.contract.openapi.cached
import org.http4k.contract.openapi.v3.ApiServer
import org.http4k.contract.openapi.v3.OpenApi3
import org.http4k.contract.ui.swaggerUiLite
import org.http4k.core.Method
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.ConnectorBuilder
import org.http4k.server.Http4kServer
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.http4k.server.http

/** Responsible for setting up the http4k server for the service's APIs. */
class ApiServer(
    private val config: Config,
    app: App,
) {
  /**
   * When creating a new API resource, make an [EndpointGroup] for it and add it here.
   * [createRouter] below will then register all the routes for the endpoints.
   */
  private val apis: List<EndpointGroup> =
      listOf(
          UserRoleApi(app.userRoleRepo),
      )

  private val log = getLogger {}

  fun start(): Http4kServer {
    val router = createRouter()
    val server = router.asServer(Jetty(config.api.serverPort, jettyConfig()))
    server.start()
    log.info { "Server started on port ${config.api.serverPort}" }
    return server
  }

  private fun createRouter(): RoutingHttpHandler {
    val (coreFilters, errorResponseRenderer) = baseApiSetup()

    val contractApi = contract {
      renderer = openApiRenderer(errorResponseRenderer)
      descriptionPath = "/docs/openapi-schema.json"

      for (api in apis) {
        for (endpoint in api.endpoints) {
          routes += endpoint.route()
        }
      }

      // PreFlightExtraction uses our endpoint contract metadata to validate the request, including
      // the body, _before_ hitting our handler. However, we sometimes want to return customized
      // error messages for invalid request bodies, so we don't want this pre-flight check.
      preFlightExtraction = PreFlightExtraction.IgnoreBody
    }

    return coreFilters
        .then(PublicExceptionFilter())
        .then(AuthFilter(config.api.credentials))
        .then(
            routes(
                "/api" bind contractApi,
                "/health" bind
                    Method.GET to
                    HealthEndpoint(config.api.serviceName, config.buildInfo),
                swaggerUiLite { url = "/api/docs/openapi-schema.json" },
            ),
        )
  }

  private fun baseApiSetup(): LifligBasicApiSetupConfig {
    return LifligBasicApiSetup(
            logHandler = LoggingFilter.createLogHandler(suppressSuccessfulHealthChecks = true),
            logHttpBody = config.api.logHttpBody,
            corsPolicy = config.api.corsPolicy,
        )
        .create(principalLog = { null })
  }

  private fun openApiRenderer(
      errorResponseRenderer: ContractLensErrorResponseRenderer
  ): OpenApi3<JsonNode> {
    val jacksonConfig = CustomJacksonConfig
    return OpenApi3(
        apiInfo =
            ApiInfo(
                title = config.api.serviceName,
                version = "v1",
                description = "REST API for ${config.api.serviceName}.",
            ),
        servers =
            listOf(
                ApiServer(Uri.of(config.api.serverBaseUrl)),
            ),
        json = jacksonConfig,
        apiRenderer =
            ApiRenderer.Auto<org.http4k.contract.openapi.v3.Api<JsonNode>, JsonNode>(
                    jacksonConfig,
                    schema = AutoJsonToJsonSchema(jacksonConfig),
                )
                .cached(),
        errorResponseRenderer = errorResponseRenderer,
    )
  }

  private fun jettyConfig(): ConnectorBuilder = { server ->
    http(config.api.serverPort)(server).apply {
      connectionFactories.filterIsInstance<HttpConnectionFactory>().forEach {
        /** Avoids leaking Jetty version in http response header "Server". */
        it.httpConfiguration.sendServerVersion = false
      }
    }
  }
}
