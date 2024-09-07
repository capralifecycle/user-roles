package no.liflig.userroles.testutils

import no.liflig.userroles.Config
import no.liflig.userroles.ServiceRegistry
import org.http4k.client.JavaHttpClient

/** Services that need to be exposed for tests */
class TestServices {
  val serverPort: Int = AvailablePortLocator.findAvailableTcpPort()
  val baseUrl = "http://localhost:${serverPort}"
  val config = Config(serverPort = serverPort)

  val registry: ServiceRegistry =
      ServiceRegistry(
          config,
          jdbi = createJdbiForTests(),
      )

  val httpClient = JavaHttpClient()

  fun clear() {
    registry.userRoleRepo.listAll().forEach { registry.userRoleRepo.delete(it.item.id, it.version) }
  }
}
