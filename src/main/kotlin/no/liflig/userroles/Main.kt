package no.liflig.userroles

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.slf4j.MDCContext
import mu.KotlinLogging
import no.liflig.http4k.asServer

val logger = KotlinLogging.logger {}

suspend fun main() {
  App.start(ServiceRegistry.default())
}

object App {
  var isRunning = false

  suspend fun start(registry: ServiceRegistry) = coroutineScope {
    launch(MDCContext()) { }

    registry.webserver.asServer(registry.webserverPort).start()
    logger.info { "Server started on ${registry.webserverPort}" }
    isRunning = true
  }
}
