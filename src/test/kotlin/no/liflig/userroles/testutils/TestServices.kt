package no.liflig.userroles.testutils

import no.liflig.userroles.Config
import no.liflig.userroles.ServiceRegistry

/** Services that need to be exposed for tests */
class TestServices {
  val serverPort: Int = AvailablePortLocator.findAvailableTcpPort()
  val serviceRegistry: ServiceRegistry =
      ServiceRegistry(
          config = Config(serverPort = serverPort),
          jdbi = createJdbiForTests(),
      )

  fun clear() {
    serviceRegistry.userRolesRepository.listAll().forEach {
      serviceRegistry.userRolesRepository.delete(it.item.id, it.version)
    }
  }
}
