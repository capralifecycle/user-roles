package no.liflig.userroles.common.config

import java.util.Properties
import no.liflig.properties.stringNotEmpty
import org.http4k.core.Method
import org.http4k.filter.AllowAll
import org.http4k.filter.AnyOf
import org.http4k.filter.CorsPolicy
import org.http4k.filter.OriginPolicy

data class CorsConfig(
    val allowedOrigins: List<String>,
    val allowedHeaders: List<String>,
    val allowedMethods: List<Method>,
) {
  fun asPolicy(): CorsPolicy =
      CorsPolicy(
          if ("*" in allowedOrigins) OriginPolicy.AllowAll()
          else OriginPolicy.AnyOf(allowedOrigins),
          allowedHeaders,
          allowedMethods,
      )

  companion object {
    fun from(properties: Properties) =
        CorsConfig(
            allowedOrigins = properties.stringNotEmpty("cors.allow.origin").split(","),
            allowedHeaders = properties.stringNotEmpty("cors.allow.headers").split(","),
            allowedMethods =
                properties.stringNotEmpty("cors.allow.methods").split(",").map(Method::valueOf),
        )
  }
}
