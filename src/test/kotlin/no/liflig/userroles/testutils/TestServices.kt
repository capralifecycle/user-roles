package no.liflig.userroles.testutils

import no.liflig.userroles.Config
import no.liflig.userroles.ServiceRegistry

/** Services that need to be exposed for tests */
class TestServices
private constructor(
    val serviceRegistry: ServiceRegistry,
    val serverPort: Int,
) {
  fun clear() {
    serviceRegistry.userRolesRepository.listAll().forEach {
      serviceRegistry.userRolesRepository.delete(it)
    }
  }

  companion object {
    fun create(): TestServices {
      val serverPort = AvailablePortLocator.findAvailableTcpPort()
      val config = Config(serverPort = serverPort)
      val jdbi = createJdbiForTests()

      val serviceRegistry =
          ServiceRegistry(
              config,
              jdbi,
          )

      return TestServices(
          serviceRegistry = serviceRegistry,
          serverPort = serverPort,
      )
    }
  }
}
