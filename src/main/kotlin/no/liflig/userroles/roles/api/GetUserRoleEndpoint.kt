package no.liflig.userroles.roles.api

import no.liflig.userroles.common.http4k.Endpoint
import no.liflig.userroles.roles.UserRoleRepository
import org.http4k.contract.ContractRoute
import org.http4k.contract.div
import org.http4k.contract.meta
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.lens.Path

/** Contains the endpoint for getting a single user role */
class GetUserRoleEndpoint(
    private val userRoleRepo: UserRoleRepository,
) : Endpoint {
  override fun route(): ContractRoute {
    val path = UserRoleApi.PATH / Path.of("username")
    val spec =
        path.meta {
          summary = "Get user role"
          description = "Username should be the user's username in our identity provider"
          returning(status = Status.OK, body = UserRoleDto.bodyLens to UserRoleDto.example)
        }
    return spec.bindContract(Method.GET) to ::handler
  }

  private fun handler(username: String) =
      fun(_: Request): Response {
        val (userRole, _) =
            userRoleRepo.getByUsername(username) ?: return Response(Status.NOT_FOUND)

        return Response(Status.OK).with(UserRoleDto.bodyLens of userRole.toDto())
      }
}
