package no.liflig.userroles.testutils

import no.liflig.userroles.Config
import no.liflig.userroles.ServiceRegistry

/** Services that need to be exposed for tests */
class TestServices {
  val serverPort: Int = AvailablePortLocator.findAvailableTcpPort()
  val config = Config(serverPort = serverPort)
  val serviceRegistry: ServiceRegistry =
      ServiceRegistry(
          config = config,
          jdbi = createJdbiForTests(),
      )

  fun clear() {
    serviceRegistry.userRolesRepository.listAll().forEach {
      serviceRegistry.userRolesRepository.delete(it.item.id, it.version)
    }
  }
}
