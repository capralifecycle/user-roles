package no.liflig.userroles

import no.liflig.http4k.ServiceRouter
import no.liflig.http4k.health.HealthService
import no.liflig.logging.RequestResponseLog
import no.liflig.userroles.common.auth.DummyAuthService
import no.liflig.userroles.common.config.http4k.UserPrincipal
import no.liflig.userroles.common.config.http4k.UserPrincipalLog
import no.liflig.userroles.common.config.http4k.createAuthFilter
import no.liflig.userroles.common.config.http4k.toLog
import no.liflig.userroles.features.userroles.app.UserRoleApi
import no.liflig.userroles.features.userroles.domain.UserRoleRepository
import org.http4k.contract.bind
import org.http4k.contract.contract
import org.http4k.contract.openapi.ApiInfo
import org.http4k.contract.openapi.v3.OpenApi3
import org.http4k.contract.ui.swaggerUi
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Uri
import org.http4k.filter.CorsPolicy
import org.http4k.routing.RoutingHttpHandler

fun createServiceRouter(
  logHandler: (RequestResponseLog<UserPrincipalLog>) -> Unit,
  healthService: HealthService,
  userRoleRepository: UserRoleRepository,
  corsConfig: CorsPolicy,
  basicAuth: BasicAuth,
): RoutingHttpHandler {
  return ServiceRouter(
    logHandler = logHandler,
    UserPrincipal::toLog,
    corsConfig,
    DummyAuthService,
    healthService,
  ) {
    logger.error(it) { "Error while retrieving principal" }
    Response(Status.UNAUTHORIZED)
  }.routingHandler {
    routes += "api" bind contract {
      renderer = OpenApi3(
        ApiInfo("User Roles API", "v1.0"),
      )
      descriptionPath = "/docs/swagger.json"
      routes += UserRoleApi("userroles", userRoleRepository).routes
    }

    routes += swaggerUi(
      Uri.of("api/docs/swagger.json"),
    )

    additionalFilters += createAuthFilter(basicAuth)
  }
}
