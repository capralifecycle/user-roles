package no.liflig.userroles.common.config

import java.util.Properties
import no.liflig.properties.booleanRequired
import no.liflig.properties.intRequired
import no.liflig.properties.stringNotEmpty
import org.http4k.core.Credentials
import org.http4k.filter.CorsPolicy

data class ApiConfig(
    val serviceName: String,
    /** Like `http://localhost`, `http://localhost:8080`, `https://myservice.prod.customer.com`. */
    val serverBaseUrl: String,
    val serverPort: Int,
    val corsPolicy: CorsPolicy,
    val credentials: Credentials,
    val logHttpBody: Boolean
) {
  companion object {
    fun from(properties: Properties) =
        ApiConfig(
            serviceName = properties.stringNotEmpty("service.name"),
            serverBaseUrl = properties.stringNotEmpty("api.baseurl"),
            serverPort = properties.intRequired("server.port"),
            corsPolicy = CorsConfig.from(properties).asPolicy(),
            credentials =
                Credentials(
                    user = properties.stringNotEmpty("basic.auth.username"),
                    password = properties.stringNotEmpty("basic.auth.password"),
                ),
            logHttpBody = properties.booleanRequired("log.http.body"),
        )
  }
}
