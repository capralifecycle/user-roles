package no.liflig.userroles.features.userroles.app.routes

import no.liflig.userroles.common.config.http4k.userIdPathLens
import no.liflig.userroles.features.userroles.domain.UserRoleRepository
import org.http4k.contract.ContractRoute
import org.http4k.contract.RouteMetaDsl
import org.http4k.contract.div
import org.http4k.contract.meta
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status

/** Contains the endpoint for deleting a user role */
class DeleteUserRole(
    private val path: String,
    private val userRoleRepository: UserRoleRepository,
) {

  private fun meta(): RouteMetaDsl.() -> Unit = {
    summary = "Delete user role"
    description = "Delete user role"
    returning(Status.OK)
  }

  fun route(): ContractRoute {
    return path / userIdPathLens meta
        meta() bindContract
        Method.DELETE to
        { id ->
          { _: Request ->
            val userRole = userRoleRepository.getByUserId(id)

            if (userRole == null) {
              Response(Status.NOT_FOUND)
            } else {
              userRoleRepository.delete(userRole)
              Response(Status.OK)
            }
          }
        }
  }
}
