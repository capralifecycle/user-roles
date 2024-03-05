package no.liflig.userroles.features.userroles.app.routes

import no.liflig.userroles.common.config.http4k.userIdPathLens
import no.liflig.userroles.features.userroles.app.UserRoleDto
import no.liflig.userroles.features.userroles.app.toDto
import no.liflig.userroles.features.userroles.domain.UserRoleRepository
import org.http4k.contract.ContractRoute
import org.http4k.contract.RouteMetaDsl
import org.http4k.contract.div
import org.http4k.contract.meta
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with

/** Contains the endpoint for getting a single user role */
class GetUserRole(
    private val path: String,
    private val userRoleRepository: UserRoleRepository,
) {

  private fun meta(): RouteMetaDsl.() -> Unit = {
    summary = "Get userRole"
    description = "Get userRole"
    returning(status = Status.OK, body = UserRoleDto.bodyLens to UserRoleDto.example)
  }

  fun route(): ContractRoute {
    return path / userIdPathLens meta
        meta() bindContract
        Method.GET to
        { id ->
          { _: Request ->
            val userRole = userRoleRepository.getByUserId(id)
            if (userRole == null) {
              Response(Status.NOT_FOUND)
            } else {
              Response(Status.OK).with(UserRoleDto.bodyLens of userRole.toDto())
            }
          }
        }
  }
}
