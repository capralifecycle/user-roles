package no.liflig.userroles.testutils

import kotlin.concurrent.thread
import no.liflig.userroles.App
import org.awaitility.Awaitility
import org.awaitility.core.ConditionTimeoutException
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ExtensionContext.Namespace.GLOBAL
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver

/**
 * JUnit extension class for use with flow/integration tests.
 *
 * Responsible for wiring up the application with test mocks/fakes, starting the application and
 * supplying tests with necessary dependencies
 */
class FlowTestExtension :
    BeforeAllCallback,
    AfterEachCallback,
    BeforeEachCallback,
    ParameterResolver,
    ExtensionContext.Store.CloseableResource {
  companion object {
    private var started = false
    private lateinit var testServices: TestServices
    private val FLOW_TEST_NAMESPACE = ExtensionContext.Namespace.create("FLOW_TEST")
  }

  override fun beforeAll(context: ExtensionContext) {
    if (!started) {
      started = true
      setupTestSuite()
      // The following line registers a callback hook when the root test context is shut down
      // The effect of this is that this startup logic will only be run once before all tests
      // that use this extension
      context.root.getStore(GLOBAL).put("flow-tests", this)
    }
  }

  private fun setupTestSuite() {
    testServices = TestServices()
    // Run application concurrently, so that it doesn't block test code
    thread { App.start(testServices.config, testServices.serviceRegistry) }
    // Make sure application is started before proceeding
    try {
      Awaitility.await().until { App.isRunning }
    } catch (e: ConditionTimeoutException) {
      throw RuntimeException(
          "Server did not start up in time for tests to run. It might not have enough time to start" +
              ", or there might be something wrong with the startup process",
          e,
      )
    }
  }

  override fun afterEach(p0: ExtensionContext?) {
    // TODO: Clear stuff here
  }

  override fun beforeEach(p0: ExtensionContext?) {
    // ignored
  }

  /**
   * Avoid using members directly when injecting into test methods because of potential problems
   * when running test in parallel. Instead, we are using the cross-test JUnit ExtensionContext
   * member store. Ref: https://stackoverflow.com/a/58586208/258510
   */
  private fun <T> getObjectFromStore(extensionContext: ExtensionContext, o: T): T {
    val returnedObject: T? =
        extensionContext.root.getStore(FLOW_TEST_NAMESPACE).get(o, o!!::class.java) as T
    return if (returnedObject == null) {
      extensionContext.root.getStore(FLOW_TEST_NAMESPACE).put(o.javaClass.canonicalName, o)
      o
    } else {
      returnedObject
    }
  }

  /**
   * Checks if a parameter type to inject into tests is supported. Extend this if more parameters
   * need to be injected into tests
   */
  override fun supportsParameter(p0: ParameterContext?, p1: ExtensionContext?): Boolean =
      p0?.parameter?.type == TestServices::class.java

  /**
   * Resolves parameter that are to be rejected into tests. If a tests attempts to use a parameter
   * type that is not defined here, the test will fail at runtime
   */
  override fun resolveParameter(p0: ParameterContext?, p1: ExtensionContext): Any? {
    return if (p0?.parameter?.type == TestServices::class.java) {
      getObjectFromStore(p1, testServices)
    } else {
      null
    }
  }

  override fun close() {
    // ignored. Should shutdown stuff
  }
}
