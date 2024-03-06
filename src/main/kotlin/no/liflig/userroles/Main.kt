package no.liflig.userroles

import mu.KotlinLogging
import no.liflig.userroles.common.config.http4k.asServer

val logger = KotlinLogging.logger {}

fun main() {
  App.start(ServiceRegistry.default())
}

object App {
  var isRunning = false

  fun start(registry: ServiceRegistry) {
    registry.webserver.asServer(registry.webserverPort).start()
    logger.info { "Server started on ${registry.webserverPort}" }
    isRunning = true
  }
}
