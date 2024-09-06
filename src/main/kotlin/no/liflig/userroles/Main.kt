package no.liflig.userroles

import mu.KotlinLogging
import no.liflig.userroles.common.config.http4k.asServer

val logger = KotlinLogging.logger {}

fun main() {
  val config = Config()
  App.start(config, ServiceRegistry(config))
}

object App {
  var isRunning = false

  fun start(config: Config, registry: ServiceRegistry) {
    val server = createApi(config, registry).asServer(config.port)
    server.start()
    logger.info { "Server started on port ${config.port}" }

    isRunning = true
  }
}
