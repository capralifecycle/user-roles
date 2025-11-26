package no.liflig.userroles

import kotlin.system.exitProcess
import no.liflig.logging.getLogger
import no.liflig.userroles.common.config.Config

private val log = getLogger()

fun main() {
  try {
    App(Config.load()).start()
  } catch (e: Throwable) {
    /**
     * If we failed to load config/start the application, we want to log it, to make sure that we
     * don't lose the exception.
     */
    log.error(e) { "Application startup failed" }
    exitProcess(status = 1)
  }
}
