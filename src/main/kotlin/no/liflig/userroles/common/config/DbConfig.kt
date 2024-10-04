package no.liflig.userroles.common.config

import java.util.Properties
import no.liflig.properties.boolean
import no.liflig.properties.intRequired
import no.liflig.properties.stringNotNull

data class DbConfig(
    val username: String,
    val password: String,
    val dbname: String,
    private val port: Int,
    private val hostname: String,
    val jdbcUrl: String = "jdbc:postgresql://$hostname:$port/$dbname",
    val cleanOnStartup: Boolean,
) {
  companion object {
    /**
     * Reads in database values that are set from an AWS Secrets Manager JSON and placed into a
     * properties file.
     */
    fun from(properties: Properties) =
        DbConfig(
            // The property keys must match the database secret JSON in AWS Secrets Manager
            username = properties.stringNotNull("database.username"),
            password = properties.stringNotNull("database.password"),
            port = properties.intRequired("database.port"),
            dbname = properties.stringNotNull("database.dbname"),
            hostname = properties.stringNotNull("database.host"),
            cleanOnStartup = properties.boolean("database.clean") ?: false,
        )
  }
}
