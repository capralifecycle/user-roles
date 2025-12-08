package no.liflig.userroles.administration.api

import no.liflig.userroles.administration.UserAdministrationService
import no.liflig.userroles.common.http4k.Endpoint
import org.http4k.contract.ContractRoute
import org.http4k.contract.div
import org.http4k.contract.meta
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.Path

class DeleteUserEndpoint(
    private val userAdministrationService: UserAdministrationService,
) : Endpoint {
  override fun route(): ContractRoute {
    val path = UserAdministrationApi.PATH / Path.of("username")
    val spec =
        path.meta {
          summary = "Delete user from identity provider, and its associated roles"
          operationId = "deleteUser"
          returning(Status.OK)
        }
    return spec.bindContract(Method.DELETE) to ::handler
  }

  private fun handler(username: String) =
      fun(_: Request): Response {
        userAdministrationService.deleteUser(username)
        return Response(Status.OK)
      }
}
