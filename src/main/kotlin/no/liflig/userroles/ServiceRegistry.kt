package no.liflig.userroles

import no.liflig.userroles.common.config.database.DatabaseConfigurator
import no.liflig.userroles.features.health.createHealthService
import no.liflig.userroles.features.userroles.UserRoleRepository
import org.jdbi.v3.core.Jdbi

/** Wires up all dependencies. */
class ServiceRegistry(
    config: Config,
    jdbi: Jdbi = createDefaultJdbi(config),
) {
  val userRolesRepository = UserRoleRepository(jdbi)

  val healthService = createHealthService(config.applicationName, config.buildInfo)
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
