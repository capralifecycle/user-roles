package no.liflig.userroles

import java.time.Instant
import java.util.*
import no.liflig.properties.boolean
import no.liflig.properties.booleanRequired
import no.liflig.properties.intRequired
import no.liflig.properties.loadProperties
import no.liflig.properties.stringNotEmpty
import no.liflig.properties.stringNotNull
import no.liflig.userroles.features.health.BuildInfo
import org.http4k.core.Credentials
import org.http4k.core.Method
import org.http4k.filter.AllowAll
import org.http4k.filter.AnyOf
import org.http4k.filter.CorsPolicy
import org.http4k.filter.OriginPolicy

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
  val environmentName =
      EnvironmentName.valueOf(properties.stringNotNull("application.env").uppercase())

  val api = ApiConfig(properties, serverPortOverride = serverPort)
  val database = DbConfig(properties)
  val buildInfo = getBuildInfo(properties, environmentName)
}

class ApiConfig(properties: Properties, serverPortOverride: Int?) {
  val applicationName = properties.stringNotEmpty("service.name")
  val serverPort = serverPortOverride ?: properties.intRequired("server.port")
  val corsPolicy = CorsConfig(properties).asPolicy()
  val credentials =
      Credentials(
          user = properties.stringNotEmpty("basic.auth.username"),
          password = properties.stringNotEmpty("basic.auth.password"),
      )
  val logHttpBody = properties.booleanRequired("log.http.body")
}

class CorsConfig(properties: Properties) {
  val allowedOrigins = properties.stringNotEmpty("cors.allow.origin").split(",")
  val allowedHeaders = properties.stringNotEmpty("cors.allow.headers").split(",")
  val allowedMethods =
      properties.stringNotEmpty("cors.allow.methods").split(",").map(Method::valueOf)

  fun asPolicy(): CorsPolicy =
      CorsPolicy(
          if ("*" in allowedOrigins) OriginPolicy.AllowAll()
          else OriginPolicy.AnyOf(allowedOrigins),
          allowedHeaders,
          allowedMethods,
      )
}

class DbConfig(properties: Properties) {
  val username = properties.stringNotNull("database.username")
  val password = properties.stringNotNull("database.password")

  private val dbname = properties.stringNotNull("database.dbname")
  private val port =
      System.getenv("DB_PORT") // Used in CI
       ?: properties.intRequired("database.port")
  private val hostname =
      System.getenv("DB_HOST") // Used in CI
       ?: properties.stringNotNull("database.host")
  val jdbcUrl = "jdbc:postgresql://$hostname:$port/$dbname"

  val cleanOnStartup = properties.boolean("database.clean") ?: false
}

enum class EnvironmentName {
  LOCAL,
  STAGING,
  DEV,
  PROD,
}

fun getBuildInfo(properties: Properties, environmentName: EnvironmentName): BuildInfo {
  try {
    return BuildInfo(
        timestamp = Instant.parse(properties.stringNotNull("build.timestamp")),
        commit = properties.stringNotNull("build.commit"),
        branch = properties.stringNotNull("build.branch"),
        number =
            try {
              properties.intRequired("build.number")
            } catch (e: IllegalArgumentException) {
              0
            },
    )
  } catch (e: Exception) {
    if (environmentName == EnvironmentName.LOCAL) {
      return BuildInfo(
          timestamp = Instant.now(),
          commit = "local",
          branch = "local",
          number = 1,
      )
    } else {
      throw e
    }
  }
}
