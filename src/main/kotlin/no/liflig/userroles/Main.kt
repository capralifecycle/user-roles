package no.liflig.userroles

fun main() {
  val config = Config()
  val registry = ServiceRegistry(config)
  startApplication(config, registry)
}

fun startApplication(config: Config, registry: ServiceRegistry) {
  ApiServer(config, registry).start()
}
