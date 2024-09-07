package no.liflig.userroles

import no.liflig.userroles.common.database.DatabaseConfigurator
import no.liflig.userroles.features.userroles.UserRoleRepository
import org.jdbi.v3.core.Jdbi

/** Wires up all dependencies. */
class ServiceRegistry(
    config: Config,
    jdbi: Jdbi = createDefaultJdbi(config),
) {
  val userRoleRepo = UserRoleRepository(jdbi)
}

private fun createDefaultJdbi(config: Config): Jdbi =
    DatabaseConfigurator.createJdbiInstanceAndMigrate(
        DatabaseConfigurator.createDataSource(
            config.database.jdbcUrl,
            config.database.username,
            config.database.password,
        ),
        cleanDatabase = config.database.cleanOnStartup,
    )
