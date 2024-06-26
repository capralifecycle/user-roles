package no.liflig.userroles

import no.liflig.documentstore.dao.CrudDaoJdbi
import no.liflig.logging.http4k.LoggingFilter
import no.liflig.userroles.common.config.database.DatabaseConfigurator
import no.liflig.userroles.common.config.http4k.UserPrincipalLog
import no.liflig.userroles.common.serialization.userRolesSerializationAdapter
import no.liflig.userroles.features.health.createHealthService
import no.liflig.userroles.features.userroles.persistence.UserRoleRepository
import no.liflig.userroles.features.userroles.persistence.UserRoleSearchDao
import org.jdbi.v3.core.Jdbi

/**
 * Wires up all dependencies.
 *
 * This class should be used close to main method and not passed around
 */
class ServiceRegistry(
    config: Config,
    jdbi: Jdbi = createDefaultJdbi(config),
) {
  val userRolesRepository =
      UserRoleRepository(
          crudDao = CrudDaoJdbi(jdbi, "userroles", userRolesSerializationAdapter),
          searchDao = UserRoleSearchDao(jdbi, "userroles"),
      )

  val webserverPort = config.port

  val webserver =
      createServiceRouter(
          logHandler =
              LoggingFilter.createLogHandler(
                  printStacktraceToConsole = true,
                  principalLogSerializer = UserPrincipalLog.serializer(),
              ),
          healthService = createHealthService(config.applicationName, config.buildInfo),
          corsConfig = config.corsPolicy,
          basicAuth = config.basicAuth,
          userRoleRepository = userRolesRepository,
      )

  companion object {
    fun default() = ServiceRegistry(Config())
  }
}

private fun createDefaultJdbi(config: Config): Jdbi =
    DatabaseConfigurator.createJdbiInstanceAndMigrate(
        DatabaseConfigurator.createDataSource(
            config.databaseConfig.jdbcUrl,
            config.databaseConfig.username,
            config.databaseConfig.password,
        ),
        config.databaseClean,
    )
