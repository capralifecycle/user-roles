package no.liflig.userroles.common.config

import java.time.Instant
import java.util.Properties
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import no.liflig.properties.intRequired
import no.liflig.properties.stringNotNull
import no.liflig.userroles.common.serialization.SerializableInstant

@Serializable
data class BuildInfo(
    val timestamp: SerializableInstant,
    /** Git commit sha. */
    val commit: String,
    /** Git branch. */
    val branch: String,
    /** CI build number. */
    val number: Int,
) {
  fun toJson(): String = Json.encodeToString(serializer(), this)

  companion object {
    /** Create [BuildInfo] based on keys in `application.properties`. */
    fun from(properties: Properties) =
        BuildInfo(
            timestamp = Instant.parse(properties.stringNotNull("build.timestamp")),
            commit = properties.stringNotNull("build.commit"),
            branch = properties.stringNotNull("build.branch"),
            number =
                try {
                  properties.intRequired("build.number")
                } catch (_: IllegalArgumentException) {
                  0
                },
        )
  }
}
