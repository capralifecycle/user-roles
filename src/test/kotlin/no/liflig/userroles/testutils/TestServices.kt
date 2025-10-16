package no.liflig.userroles.testutils

import no.liflig.userroles.App
import no.liflig.userroles.common.config.Config
import org.http4k.client.JavaHttpClient
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

/**
 * Application services for use in integration tests.
 *
 * [TestServices.get] returns a singleton instance, which runs the application side by side with
 * your tests, so you can test against a real running app. [TestServices.clear] is called before
 * each test, to clear any state between tests.
 *
 * To use this in your test class, add a field as follows:
 * ```
 * import org.junit.jupiter.api.extension.RegisterExtension
 *
 * class ExampleTest {
 *   // `RegisterExtension` makes it so that `TestServices.clear` is called before each test
 *   @RegisterExtension private val services = TestServices.get()
 *
 *   // Use `services` in your test methods
 * }
 * ```
 */
class TestServices private constructor() : BeforeEachCallback {
  companion object {
    /** See [TestServices]. */
    fun get(): TestServices = INSTANCE

    /** Singleton instance, to make sure that we only run 1 instance of the app at a time. */
    private val INSTANCE: TestServices by lazy {
      val services = TestServices()
      services.app.start()
      return@lazy services
    }
  }

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

  override fun beforeEach(context: ExtensionContext) {
    this.clear()
  }
}
