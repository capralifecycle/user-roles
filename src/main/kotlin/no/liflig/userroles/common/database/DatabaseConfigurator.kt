package no.liflig.userroles.common.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.util.IsolationLevel
import java.sql.Connection
import java.sql.SQLException
import java.util.concurrent.TimeUnit
import javax.sql.DataSource
import kotlin.concurrent.thread
import no.liflig.documentstore.DocumentStorePlugin
import no.liflig.logging.getLogger
import no.liflig.userroles.common.database.DatabaseConfigurator.createJdbiInstanceAndMigrate
import no.liflig.userroles.common.observability.OpenTelemetryConfig
import no.liflig.userroles.common.observability.use
import org.flywaydb.core.Flyway
import org.jdbi.v3.core.ConnectionFactory
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.core.transaction.SerializableTransactionRunner

/**
 * Creates new instances of JDBI ready to use in your application with
 * [createJdbiInstanceAndMigrate].
 */
object DatabaseConfigurator {
  private val log = getLogger {}

  /**
   * Defined by `cpuCount * 2 + 2`.
   *
   * Anywhere between `cpuCount * 2` to `cpuCount * 4` may be a good spot.
   *
   * Note that postgres could cap out at 160 total connections, making you unable to deploy new
   * instances if you are scaling up. RDS scales it with the instance memory (RAM). Get the exact
   * value with `select * from pg_settings where name='max_connections';`
   */
  private const val CONNECTION_POOL_MAXIMUM_SIZE = 10

  /**
   * Creates a new JDBI instance with a Connection Pool, and automatically migrates the database.
   *
   * Flyway Database Migrations are read from `src/main/resources/migrations/`, and must be named
   * using the Flyway naming schema
   *
   * Call this once and store the result. Reuse this JDBI instance everywhere in your application.
   * Calling this method creates another connection pool and migration every time.
   */
  fun createJdbiInstanceAndMigrate(
      /** The jdbc database url, like `"jdbc:postgresql://localhost:5432/myDatabase"`. */
      url: String,
      /** Database username, like `"app"`. */
      username: String,
      /** Database password, like `"hunter2"`. */
      password: String,
      /**
       * A human-readable name for the Connection pool. Shows up in metrics for pool usage. Useful
       * if you have several connection pools.
       */
      connectionPoolName: String = "default",
      cleanDatabase: Boolean = false,
  ): Jdbi {
    val dataSource =
        createDataSource(url = url, username = username, password = password, connectionPoolName)
    val jdbi =
        Jdbi.create(PostgresConnectionFactory(dataSource))
            // The plugin configures mappings for some default types https://jdbi.org/#_postgresql
            .installPlugin(KotlinPlugin())
            .installPlugin(DocumentStorePlugin())

    // This handler will auto retry failures if you use
    // `jdbi.useTransaction<RuntimeException>(TransactionIsolationLevel.SERIALIZABLE) { handle ->
    // myCode } `.
    // Note that YOUR code inside `.inTransaction { myCode }` will be retried and called multiple
    // times!
    jdbi.transactionHandler = SerializableTransactionRunner()

    // Run database migrations
    migrate(dataSource, cleanDatabase)
    return jdbi
  }

  private fun createDataSource(
      url: String,
      username: String,
      password: String,
      name: String,
  ): DataSource {
    val config =
        HikariConfig().apply {
          // The System property is for overriding the driver in tests, etc.
          driverClassName = System.getProperty("database.driver.class", "org.postgresql.Driver")
          jdbcUrl = url
          this.username = username
          this.password = password

          /*
           - If you are using AWS API-GW, you can set 30s here, as it hard-caps responses to 30s.
           - If the connection is used for SQS processing, set it close or below SQS Message Visibility Timeout.
           - If you can wait however much you like for a connection, put a high value like 10min.
          */
          connectionTimeout = TimeUnit.SECONDS.toMillis(60L)

          // We choose the default READ_COMMITTED to avoid dealing with serialization failures.
          // See https://www.postgresql.org/docs/current/transaction-iso.html
          transactionIsolation = IsolationLevel.TRANSACTION_READ_COMMITTED.name

          // Not very relevant if minimum == maximum.
          idleTimeout = TimeUnit.MINUTES.toMillis(10L)
          minimumIdle = CONNECTION_POOL_MAXIMUM_SIZE
          maximumPoolSize = CONNECTION_POOL_MAXIMUM_SIZE

          this.poolName = name
        }

    val dataSource = HikariDataSource(config)

    try {
      // Important! Otherwise, Postgres may keep the connections
      // even after this server has shut down
      Runtime.getRuntime()
          .addShutdownHook(
              thread(start = false, name = "hikari-shutdown") {
                if (!dataSource.isClosed) dataSource.close()
              })
    } catch (e: Exception) {
      log.error(e) { "Failed to add shutdown hook for hikari." }
    }

    return dataSource
  }

  private fun migrate(dataSource: DataSource, cleanDatabase: Boolean) {
    val flyway =
        Flyway.configure()
            .cleanDisabled(!cleanDatabase)
            .baselineOnMigrate(true)
            .dataSource(dataSource)
            .locations("migrations")
            .load()
    /*
    Instead of SQL-file migrations, you can also extend BaseJavaMigration and define your migration as code.
    Read "Java-based migrations" at https://documentation.red-gate.com/fd/migrations-184127470.html#sql-based-migrations
    for more.
     */

    if (cleanDatabase) {
      if (dataSource.isProd()) {
        log.warn {
          "You are trying to clean the PROD database. Please contact admin if you like to proceed with this process."
        }
      } else {
        log.warn { "Cleaning database before running migrations.." }
        flyway.clean()
      }
    }

    log.debug { "Running database migrations..." }
    OpenTelemetryConfig.tracer.spanBuilder("database migrate").use { flyway.migrate() }
  }

  private fun DataSource.isProd(): Boolean = connection.toString().contains("prod")
}

/**
 * This does an extra step of executing `"DISCARD ALL"` before returning the connection to the pool.
 */
private class PostgresConnectionFactory(private val dataSource: DataSource) : ConnectionFactory {
  override fun openConnection(): Connection = dataSource.connection

  override fun closeConnection(conn: Connection) {
    postgresResetConnectionState(conn)
    conn.close()
  }

  private fun postgresResetConnectionState(conn: Connection) {
    try {
      conn.createStatement().use { statement ->
        // https://www.postgresql.org/docs/current/sql-discard.html
        statement.execute("DISCARD ALL;")
      }
    } catch (ignored: SQLException) {}
  }
}
