package no.liflig.userroles.common.config.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import mu.KotlinLogging
import no.liflig.documentstore.entity.UnmappedEntityIdArgumentFactory
import no.liflig.documentstore.entity.UuidEntityIdArgumentFactory
import no.liflig.documentstore.entity.VersionArgumentFactory
import org.flywaydb.core.Flyway
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import javax.sql.DataSource

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
    val jdbi: Jdbi = Jdbi.create(dataSource)
      .installPlugin(KotlinPlugin())
      .registerArgument(UuidEntityIdArgumentFactory())
      .registerArgument(UnmappedEntityIdArgumentFactory())
      .registerArgument(VersionArgumentFactory())
      .registerArrayType(no.liflig.documentstore.entity.EntityId::class.java, "uuid")

    migrate(dataSource, cleanDatabase)

    return jdbi
  }

  private fun migrate(dataSource: DataSource, cleanDatabase: Boolean) {
    val flyway = Flyway.configure().cleanDisabled(!cleanDatabase)
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
