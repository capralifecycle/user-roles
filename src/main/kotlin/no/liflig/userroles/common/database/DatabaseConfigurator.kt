package no.liflig.userroles.common.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource
import mu.KotlinLogging
import no.liflig.documentstore.DocumentStorePlugin
import org.flywaydb.core.Flyway
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin

private val logger = KotlinLogging.logger {}

object DatabaseConfigurator {
  fun createDataSource(
      jdbcUrl: String,
      username: String,
      password: String,
  ): HikariDataSource {
    val config = HikariConfig()
    config.jdbcUrl = jdbcUrl
    config.driverClassName = "org.postgresql.Driver"
    config.username = username
    config.password = password

    return HikariDataSource(config)
  }

  fun createJdbiInstanceAndMigrate(
      dataSource: DataSource,
      cleanDatabase: Boolean = false,
  ): Jdbi {
    val jdbi: Jdbi =
        Jdbi.create(dataSource).installPlugin(KotlinPlugin()).installPlugin(DocumentStorePlugin())

    migrate(dataSource, cleanDatabase)

    return jdbi
  }

  private fun migrate(dataSource: DataSource, cleanDatabase: Boolean) {
    val flyway =
        Flyway.configure()
            .cleanDisabled(!cleanDatabase)
            .dataSource(dataSource)
            .locations("database/migration")
            .load()

    if (cleanDatabase) {
      logger.warn("Cleaning database before running migrations")
      flyway.clean()
    }

    logger.info("Running database migrations")
    flyway.migrate()
  }
}
