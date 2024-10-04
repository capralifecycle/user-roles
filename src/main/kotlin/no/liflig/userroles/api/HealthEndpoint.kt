@file:UseSerializers(InstantSerializer::class)

package no.liflig.userroles.api

import java.lang.management.ManagementFactory
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import no.liflig.userroles.common.config.BuildInfo
import no.liflig.userroles.common.serialization.InstantSerializer
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.format.KotlinxSerialization

class HealthEndpoint(
    private val serviceName: String,
    private val buildInfo: BuildInfo,
) : HttpHandler {
  private val runningSince = getRunningSince()

  override operator fun invoke(request: Request): Response {
    val status =
        HealthStatus(
            name = serviceName,
            timestamp = Instant.now(),
            runningSince = runningSince,
            build = buildInfo,
        )
    return Response(Status.OK).with(HealthStatus.bodyLens.of(status))
  }

  private fun getRunningSince(): Instant {
    val uptimeInMillis = ManagementFactory.getRuntimeMXBean().uptime
    return Instant.now().minus(uptimeInMillis, ChronoUnit.MILLIS)
  }
}

@Serializable
data class HealthStatus(
    val name: String,
    val timestamp: Instant,
    val runningSince: Instant,
    val build: BuildInfo,
) {
  companion object {
    val bodyLens = KotlinxSerialization.autoBody<HealthStatus>().toLens()
  }
}
