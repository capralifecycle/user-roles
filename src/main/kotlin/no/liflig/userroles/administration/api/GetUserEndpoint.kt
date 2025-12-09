package no.liflig.userroles.administration.api

import no.liflig.http4k.setup.createJsonBodyLens
import no.liflig.userroles.administration.UserAdministrationService
import no.liflig.userroles.administration.UserAdministrationService.Companion.IDENTITY_PROVIDER_NAME
import no.liflig.userroles.administration.UserDataWithRoles
import no.liflig.userroles.common.http4k.Endpoint
import org.http4k.contract.ContractRoute
import org.http4k.contract.div
import org.http4k.contract.meta
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.lens.Path

class GetUserEndpoint(
    private val userAdministrationService: UserAdministrationService,
) : Endpoint {
  override fun route(): ContractRoute {
    val path = UserAdministrationApi.PATH / Path.of("username")
    val spec =
        path.meta {
          summary =
              "Get user by username from identity provider (${IDENTITY_PROVIDER_NAME}), with associated roles"
          operationId = "getUser"
          returning(Status.OK, responseBodyLens to EXAMPLE_USER_DATA_WITH_ROLES)
        }
    return spec.bindContract(Method.GET) to ::handler
  }

  private fun handler(username: String) =
      fun(_: Request): Response {
        val user = userAdministrationService.getUser(username = username)

        return Response(Status.OK).with(responseBodyLens.of(user))
      }

  companion object {
    private val responseBodyLens = createJsonBodyLens(UserDataWithRoles.serializer())
  }
}
