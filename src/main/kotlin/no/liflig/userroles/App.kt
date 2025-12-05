package no.liflig.userroles

import no.liflig.logging.getLogger
import no.liflig.userroles.administration.CognitoClientWrapper
import no.liflig.userroles.administration.CognitoClientWrapperImpl
import no.liflig.userroles.administration.UserAdministrationService
import no.liflig.userroles.api.ApiServer
import no.liflig.userroles.common.config.Config
import no.liflig.userroles.common.database.DatabaseConfigurator
import no.liflig.userroles.roles.UserRoleRepository
import org.jdbi.v3.core.Jdbi

private val log = getLogger()

/** Wires up all dependencies for the application. */
class App(
    private val config: Config,
    val jdbi: Jdbi = createJdbiInstance(config),
    val cognitoClientWrapper: CognitoClientWrapper =
        CognitoClientWrapperImpl(
            userPoolId = config.cognitoUserPoolId,
        ),
) {
  val userRoleRepo = UserRoleRepository(jdbi)

  val userAdministrationService =
      UserAdministrationService(
          userRoleRepo = userRoleRepo,
          cognitoClientWrapper = cognitoClientWrapper,
      )

  /** Must be initialized after all other dependencies. */
  val apiServer = ApiServer(config, this)

  fun start() {
    log.info {
      field("buildInfo", config.buildInfo)
      "Starting application"
    }

    apiServer.start()
  }
}

private fun createJdbiInstance(config: Config): Jdbi {
  return DatabaseConfigurator.createJdbiInstanceAndMigrate(
      url = config.database.jdbcUrl,
      username = config.database.username,
      password = config.database.password,
      cleanDatabase = config.database.cleanOnStartup,
  )
}
