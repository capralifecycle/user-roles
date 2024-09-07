package no.liflig.userroles.common.http4k

import org.eclipse.jetty.server.HttpConnectionFactory
import org.http4k.server.ConnectorBuilder
import org.http4k.server.http

fun serverConfig(port: Int): ConnectorBuilder = { server ->
  http(port)(server).apply {
    connectionFactories.filterIsInstance<HttpConnectionFactory>().forEach {
      // Avoid leaking Jetty version in http response header "Server".
      it.httpConfiguration.sendServerVersion = false
    }
  }
}
