package no.liflig.userroles.common.config.http4k

import org.eclipse.jetty.server.HttpConnectionFactory
import org.http4k.routing.RoutingHttpHandler
import org.http4k.server.ConnectorBuilder
import org.http4k.server.Http4kServer
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.http4k.server.http

fun RoutingHttpHandler.asServer(developmentPort: Int): Http4kServer {
  val port = getPortToListenOn(developmentPort)
  return asServer(Jetty(port, httpNoServerVersionHeader(port)))
}

private fun getPortToListenOn(developmentPort: Int): Int {
  val servicePortFromEnv = System.getenv("SERVICE_PORT")
  return servicePortFromEnv?.toInt() ?: developmentPort
}

// Avoid leaking Jetty version in http response header "Server".
private fun httpNoServerVersionHeader(port: Int): ConnectorBuilder = { server ->
  http(port)(server).apply {
    connectionFactories.filterIsInstance<HttpConnectionFactory>().forEach {
      it.httpConfiguration.sendServerVersion = false
    }
  }
}
