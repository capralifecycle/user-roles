@file:UseSerializers(InstantSerializer::class)

package no.liflig.userroles.features.health

import java.time.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import no.liflig.userroles.common.serialization.InstantSerializer

@Serializable
data class HealthStatus(
    val name: String,
    val timestamp: Instant,
    val runningSince: Instant,
    val build: HealthBuildInfo,
)

@Serializable
data class HealthBuildInfo(
    /** During local development this will be null. */
    val timestamp: Instant?,
    val commit: String,
    val branch: String,
    val number: Int,
)
