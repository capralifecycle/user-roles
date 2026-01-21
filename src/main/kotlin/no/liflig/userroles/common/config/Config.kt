package no.liflig.userroles.common.config

import java.util.Properties
import no.liflig.properties.loadProperties
import no.liflig.properties.string
import no.liflig.properties.stringNotNull

/**
 * Holds configuration of the service.
 *
 * @see [Config.load]
 */
data class Config(
    private val properties: Properties,
    val buildInfo: BuildInfo = BuildInfo.from(properties),
    val api: ApiConfig = ApiConfig.from(properties),
    val database: DbConfig = DbConfig.from(properties),
) {
  @Suppress("unused")
  val environmentName =
      EnvironmentName.valueOf(properties.stringNotNull("application.env").uppercase())

  val cognitoUserPoolId = properties.string("aws.cognito.userPoolId")

  companion object {
    /**
     * Creates a new instance based on `application.properties` and AWS Parameter Store (if
     * available).
     */
    fun load() = Config(loadProperties())
  }
}

enum class EnvironmentName {
  LOCAL,
  STAGING,
  DEV,
  PROD,
}
