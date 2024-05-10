package no.liflig.userroles.features.userroles.app.routes

import no.liflig.userroles.common.Endpoint
import no.liflig.userroles.common.config.http4k.userIdPathLens
import no.liflig.userroles.features.userroles.persistence.UserRoleRepository
import org.http4k.contract.ContractRoute
import org.http4k.contract.div
import org.http4k.contract.meta
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status

/** Contains the endpoint for deleting a user role */
class DeleteUserRole(
    private val userRoleRepository: UserRoleRepository,
) : Endpoint {
  override fun route(basePath: String): ContractRoute {
    val path = basePath / userIdPathLens
    val spec =
        path meta
            {
              summary = "Delete user role"
              description = "Delete user role"
              returning(Status.OK)
            }
    return spec bindContract Method.DELETE to ::handler
  }

  private fun handler(userId: String) =
      fun(_: Request): Response {
        val userRole = userRoleRepository.getByUserId(userId) ?: return Response(Status.NOT_FOUND)
        userRoleRepository.delete(userRole.id, userRole.version)
        return Response(Status.OK)
      }
}
