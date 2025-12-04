package no.liflig.userroles.api

import com.fasterxml.jackson.databind.JsonNode
import no.liflig.http4k.setup.LifligBasicApiSetup
import no.liflig.http4k.setup.LifligBasicApiSetupConfig
import no.liflig.http4k.setup.errorhandling.ContractLensErrorResponseRenderer
import no.liflig.http4k.setup.logging.LoggingFilter
import no.liflig.logging.getLogger
import no.liflig.userroles.App
import no.liflig.userroles.common.config.ApiConfig
import no.liflig.userroles.common.config.Config
import no.liflig.userroles.common.http4k.BasicAuthFilter
import no.liflig.userroles.common.http4k.CustomJacksonConfig
import no.liflig.userroles.common.http4k.EndpointGroup
import no.liflig.userroles.roles.api.UserRoleApi
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
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
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

private val log = getLogger()

/**
 * Responsible for setting up the http4k server for the service's APIs.
 *
 * Implements [HttpHandler], so you can call the API directly in tests without going through a real
 * HTTP request.
 */
class ApiServer(
    private val config: Config,
    app: App,
) : HttpHandler {
  private val router: RoutingHttpHandler

  init {
    /**
     * When creating a new API resource, make an [EndpointGroup] for it and add it here. We loop
     * through this below to register all the routes for our endpoints.
     */
    val apis: List<EndpointGroup> =
        listOf(
            UserRoleApi(app.userRoleRepo),
        )

    val (coreFilters, errorResponseRenderer) = baseApiSetup(config.api)

    val contractApi = contract {
      renderer = openApiRenderer(config.api, errorResponseRenderer)
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

    router =
        coreFilters
            .then(BasicAuthFilter(config.api.credentials))
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

  fun start(): Http4kServer {
    val server = router.asServer(Jetty(config.api.serverPort, jettyConfig()))
    server.start()
    log.info { "Server started on port ${config.api.serverPort}" }
    return server
  }

  override fun invoke(request: Request): Response = router(request)

  private fun jettyConfig(): ConnectorBuilder = { server ->
    http(config.api.serverPort)(server).apply {
      connectionFactories.filterIsInstance<HttpConnectionFactory>().forEach {
        /** Avoids leaking Jetty version in http response header "Server". */
        it.httpConfiguration.sendServerVersion = false
      }
    }
  }
}

private fun baseApiSetup(config: ApiConfig): LifligBasicApiSetupConfig {
  return LifligBasicApiSetup(
          logHandler = LoggingFilter.createLogHandler(suppressSuccessfulHealthChecks = true),
          logHttpBody = config.logHttpBody,
          corsPolicy = config.corsPolicy,
      )
      .create(principalLog = { null })
}

private fun openApiRenderer(
    config: ApiConfig,
    errorResponseRenderer: ContractLensErrorResponseRenderer,
): OpenApi3<JsonNode> {
  val jacksonConfig = CustomJacksonConfig
  return OpenApi3(
      apiInfo =
          ApiInfo(
              title = config.serviceName,
              version = "v1",
              description = "REST API for ${config.serviceName}.",
          ),
      servers =
          listOf(
              ApiServer(Uri.of(config.serverBaseUrl)),
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
