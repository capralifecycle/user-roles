package no.liflig.userroles.roles.api

import no.liflig.userroles.common.http4k.Endpoint
import no.liflig.userroles.common.http4k.userIdPathLens
import no.liflig.userroles.roles.UserRoleRepository
import org.http4k.contract.ContractRoute
import org.http4k.contract.div
import org.http4k.contract.meta
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status

/** Contains the endpoint for deleting a user role */
class DeleteUserRoleEndpoint(
    private val userRoleRepo: UserRoleRepository,
) : Endpoint {
  override fun route(): ContractRoute {
    val path = UserRoleApi.PATH / userIdPathLens
    val spec =
        path.meta {
          summary = "Delete user role"
          description = "Delete user role"
          returning(Status.OK)
        }
    return spec.bindContract(Method.DELETE) to ::handler
  }

  private fun handler(userId: String) =
      fun(_: Request): Response {
        val (userRole, version) =
            userRoleRepo.getByUserId(userId) ?: return Response(Status.NOT_FOUND)
        userRoleRepo.delete(userRole.id, version)
        return Response(Status.OK)
      }
}
