package no.liflig.userroles

import mu.KotlinLogging
import net.logstash.logback.marker.Markers.appendRaw
import no.liflig.userroles.api.ApiServer
import no.liflig.userroles.common.config.Config
import no.liflig.userroles.common.database.DatabaseConfigurator
import no.liflig.userroles.features.userroles.UserRoleRepository
import org.jdbi.v3.core.Jdbi

private val log = KotlinLogging.logger {}

/** Wires up all dependencies for the application. */
class App(
    private val config: Config,
    jdbi: Jdbi = createJdbiInstance(config),
) {
  val userRoleRepo = UserRoleRepository(jdbi)

  fun start() {
    log.info(appendRaw("buildInfo", config.buildInfo.toJson())) { "Starting application" }

    ApiServer(config, this).start()
  }
}

private fun createJdbiInstance(config: Config): Jdbi {
  return DatabaseConfigurator.createJdbiInstanceAndMigrate(
      url = config.database.jdbcUrl,
      username = config.database.username,
      password = config.database.password,
      cleanDatabase = config.database.cleanOnStartup,
  )
}