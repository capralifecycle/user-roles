package no.liflig.userroles

import java.time.Instant
import java.util.*
import no.liflig.properties.boolean
import no.liflig.properties.intRequired
import no.liflig.properties.loadProperties
import no.liflig.properties.stringNotEmpty
import no.liflig.properties.stringNotNull
import no.liflig.userroles.common.config.http4k.CorsConfig
import no.liflig.userroles.features.health.HealthBuildInfo

/**
 * Parsing of properties into configuration.
 *
 * This class should only be used in the outer part of the application, such as close to the main
 * method.
 *
 * The purpose of this class is to encapsulate loading and parsing of properties.
 */
class Config(
    properties: Properties = loadProperties(),
    serverPort: Int? = null,
) {

  val applicationName = "user-roles"

  val corsPolicy = CorsConfig.from(properties).asPolicy()

  val port = serverPort ?: properties.intRequired("server.port")

  val environmentName =
      no.liflig.userroles.EnvironmentName.valueOf(
          properties.stringNotNull("application.env").uppercase(),
      )

  val buildInfo = properties.getHealthBuildInfo()

  val basicAuth =
      BasicAuth(
          properties.stringNotEmpty("basic.auth.username"),
          properties.stringNotEmpty("basic.auth.password"),
      )

  private fun Properties.getHealthBuildInfo() =
      kotlin
          .runCatching {
            HealthBuildInfo(
                timestamp = Instant.parse(stringNotNull("build.timestamp")),
                commit = stringNotNull("build.commit"),
                branch = stringNotNull("build.branch"),
                number = 1,
            )
          }
          .getOrElse {
            if (environmentName == EnvironmentName.LOCAL) {
              HealthBuildInfo(
                  timestamp = Instant.now(),
                  commit = "local",
                  branch = "local",
                  number = 1,
              )
            } else {
              throw it
            }
          }

  val databaseConfig: DbConfig = DbConfig.from(properties)
  val databaseClean: Boolean = properties.boolean("database.clean") ?: false
}

class BasicAuth(private val username: String, private val password: String) {
  fun header() = "Basic " + Base64.getEncoder().encodeToString("$username:$password".toByteArray())
}

enum class EnvironmentName {
  LOCAL,
  STAGING,
  DEV,
  PROD,
}

data class DbConfig(
    val jdbcUrl: String,
    val username: String,
    val password: String,
) {
  companion object {
    fun from(properties: Properties): DbConfig {
      val port =
          System.getenv("DB_PORT") // Used in CI
           ?: properties.intRequired("database.port")

      val hostname =
          System.getenv("DB_HOST") // Used in CI
           ?: properties.stringNotNull("database.host")

      val dbname = properties.stringNotEmpty("database.dbname")
      val username = properties.stringNotEmpty("database.username")
      val password = properties.stringNotEmpty("database.password")
      val jdbcUri = "jdbc:postgresql://$hostname:$port/$dbname"

      return DbConfig(jdbcUri, username, password)
    }
  }
}
