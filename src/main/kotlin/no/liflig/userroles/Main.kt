package no.liflig.userroles

import no.liflig.userroles.common.config.Config

fun main() {
  App(Config.load()).start()
}
