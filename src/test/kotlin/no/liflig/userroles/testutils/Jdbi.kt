package no.liflig.userroles.testutils

import no.liflig.userroles.common.config.database.DatabaseConfigurator
import org.jdbi.v3.core.Jdbi
import org.testcontainers.containers.PostgreSQLContainer

class KPostgreSQLContainer(imageName: String) :
    PostgreSQLContainer<KPostgreSQLContainer>(imageName)

fun createJdbiForTests(): Jdbi {
  val username = "user"
  val password = "password"
  val imageName = "postgres:15.5"
  val pgContainer = KPostgreSQLContainer(imageName)

  pgContainer.withDatabaseName("userrolesdb").withUsername(username).withPassword(password).start()

  // Unsure if we need other db driver in the Hikari config during tests
  return DatabaseConfigurator.createJdbiInstanceAndMigrate(
      DatabaseConfigurator.createDataSource(
          pgContainer.jdbcUrl,
          username,
          password,
      ),
  )
}
