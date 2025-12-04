package no.liflig.userroles.testutils

import no.liflig.documentstore.repository.useHandle
import no.liflig.userroles.App
import no.liflig.userroles.administration.MockCognitoClientWrapper
import no.liflig.userroles.common.config.Config
import no.liflig.userroles.roles.UserRoleRepository
import org.http4k.core.HttpHandler
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient

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

  val jdbi = createJdbiForTests()

  val cognitoClientWrapper = MockCognitoClientWrapper()

  val app =
      App(
          config,
          jdbi = jdbi,
          cognitoClientWrapper = cognitoClientWrapper,
      )

  /**
   * We expose the http4k `RoutingHttpHandler` from our `ApiServer` here, so that we can make
   * requests to our API without going through an actual HTTP request. This speeds up our tests.
   *
   * Our `HealthEndpointTest` tests with a real HTTP request, so that we have at least 1 test that
   * checks that our HTTP server setup works.
   */
  val apiClient: HttpHandler
    get() = app.apiServer

  fun clear() {
    truncateTable(UserRoleRepository.TABLE_NAME)

    cognitoClientWrapper.reset()
  }

  override fun beforeEach(context: ExtensionContext) {
    this.clear()
  }

  /**
   * Use [no.liflig.userroles.administration.MockCognitoClient] as a base class for mocking this.
   */
  fun mockCognito(client: CognitoIdentityProviderClient) {
    cognitoClientWrapper.cognitoClient = client
  }

  private fun truncateTable(tableName: String) {
    useHandle(jdbi) { handle ->
      handle.createUpdate("TRUNCATE TABLE \"${tableName}\" CASCADE").execute()
    }
  }
}
