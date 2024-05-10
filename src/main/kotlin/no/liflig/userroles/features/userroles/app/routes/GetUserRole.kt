package no.liflig.userroles.features.userroles.app.routes

import no.liflig.userroles.common.Endpoint
import no.liflig.userroles.common.config.http4k.userIdPathLens
import no.liflig.userroles.features.userroles.app.UserRoleDto
import no.liflig.userroles.features.userroles.app.toDto
import no.liflig.userroles.features.userroles.persistence.UserRoleRepository
import org.http4k.contract.ContractRoute
import org.http4k.contract.div
import org.http4k.contract.meta
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with

/** Contains the endpoint for getting a single user role */
class GetUserRole(
    private val userRoleRepository: UserRoleRepository,
) : Endpoint {
  override fun route(basePath: String): ContractRoute {
    val path = basePath / userIdPathLens
    val spec =
        path meta
            {
              summary = "Get userRole"
              description = "Get userRole"
              returning(status = Status.OK, body = UserRoleDto.bodyLens to UserRoleDto.example)
            }
    return spec bindContract Method.GET to ::handler
  }

  private fun handler(userId: String) =
      fun(_: Request): Response {
        val userRole = userRoleRepository.getByUserId(userId) ?: return Response(Status.NOT_FOUND)
        return Response(Status.OK).with(UserRoleDto.bodyLens of userRole.toDto())
      }
}
