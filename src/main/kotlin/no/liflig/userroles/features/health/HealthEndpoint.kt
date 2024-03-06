package no.liflig.userroles.features.health

import org.http4k.core.Body
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.format.KotlinxSerialization.auto
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind

private val jsonLens = Body.auto<HealthStatus>().toLens()

fun healthEndpoint(healthService: HealthService): RoutingHttpHandler =
    "health" bind
        Method.GET to
        {
          Response(Status.OK).with(jsonLens of healthService.healthStatus())
        }
