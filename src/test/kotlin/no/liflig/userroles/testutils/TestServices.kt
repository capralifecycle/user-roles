package no.liflig.userroles.testutils

import no.liflig.userroles.App
import no.liflig.userroles.common.config.Config
import org.http4k.client.JavaHttpClient

/** Services that need to be exposed for tests */
class TestServices {
  val serverPort: Int = AvailablePortLocator.findAvailableTcpPort()
  val baseUrl = "http://localhost:${serverPort}"
  val config =
      Config.load().let { config ->
        config.copy(
            api = config.api.copy(serverPort = serverPort),
        )
      }

  val app =
      App(
          config,
          jdbi = createJdbiForTests(),
      )

  val httpClient = JavaHttpClient()

  fun clear() {
    app.userRoleRepo.listAll().forEach { app.userRoleRepo.delete(it.data.id, it.version) }
  }
}
