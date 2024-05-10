package no.liflig.userroles.features.userroles.app.routes

import no.liflig.userroles.common.Endpoint
import no.liflig.userroles.common.config.http4k.createBodyLens
import no.liflig.userroles.common.config.http4k.userIdPathLens
import no.liflig.userroles.features.userroles.app.UserRoleDto
import no.liflig.userroles.features.userroles.app.exampleRole
import no.liflig.userroles.features.userroles.app.toDto
import no.liflig.userroles.features.userroles.domain.Role
import no.liflig.userroles.features.userroles.domain.UserRole
import no.liflig.userroles.features.userroles.persistence.UserRoleRepository
import org.http4k.contract.ContractRoute
import org.http4k.contract.div
import org.http4k.contract.meta
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with

/** Contains the endpoint for updating a user role */
class UpdateUserRole(
    private val userRoleRepository: UserRoleRepository,
) : Endpoint {
  @kotlinx.serialization.Serializable
  data class UpdateRoleRequest(
      val roles: List<Role>,
  ) {
    companion object {
      val bodyLens = createBodyLens(serializer())
      val example = UpdateRoleRequest(roles = listOf(exampleRole))
    }
  }

  override fun route(basePath: String): ContractRoute {
    val path = basePath / userIdPathLens
    val spec =
        path meta
            {
              summary = "Update user role"
              description = "Update user role"
              receiving(body = UpdateRoleRequest.bodyLens to UpdateRoleRequest.example)
              returning(Status.OK, body = UserRoleDto.bodyLens to UserRoleDto.example)
            }
    return spec bindContract Method.PUT to ::handler
  }

  private fun handler(userId: String) =
      fun(request: Request): Response {
        val body = UpdateRoleRequest.bodyLens(request)

        val existingUserRole = userRoleRepository.getByUserId(userId)
        if (existingUserRole == null) {
          val createdUserRole =
              userRoleRepository.create(UserRole(userId = userId, roles = body.roles))
          return Response(Status.OK).with(UserRoleDto.bodyLens of createdUserRole.item.toDto())
        } else {
          val updatedUserRole = existingUserRole.item.copy(roles = body.roles)
          val f = userRoleRepository.update(updatedUserRole, existingUserRole.version)

          return Response(Status.OK).with(UserRoleDto.bodyLens of f.item.toDto())
        }
      }
}
