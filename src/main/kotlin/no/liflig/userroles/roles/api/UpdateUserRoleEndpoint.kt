package no.liflig.userroles.roles.api

import kotlinx.serialization.Serializable
import no.liflig.http4k.setup.createJsonBodyLens
import no.liflig.userroles.common.http4k.Endpoint
import no.liflig.userroles.roles.Role
import no.liflig.userroles.roles.UserRole
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

/** Contains the endpoint for updating a user role */
class UpdateUserRoleEndpoint(
    private val userRoleRepo: UserRoleRepository,
) : Endpoint {
  override fun route(): ContractRoute {
    val path = UserRoleApi.PATH / Path.of("username")
    val spec =
        path.meta {
          summary = "Update user role"
          description = "Update user role"
          receiving(body = UpdateRoleRequest.bodyLens to UpdateRoleRequest.example)
          returning(Status.OK, body = UserRoleDto.bodyLens to UserRoleDto.example)
        }
    return spec.bindContract(Method.PUT) to ::handler
  }

  private fun handler(username: String) =
      fun(request: Request): Response {
        val body = UpdateRoleRequest.bodyLens(request)

        val existingUserRole = userRoleRepo.getByUsername(username)

        if (existingUserRole == null) {
          val createdUserRole =
              userRoleRepo.create(UserRole(username = username, roles = body.roles))

          return Response(Status.OK).with(UserRoleDto.bodyLens of createdUserRole.data.toDto())
        } else {
          val updatedUserRole =
              userRoleRepo.update(existingUserRole.map { it.copy(roles = body.roles) })

          return Response(Status.OK).with(UserRoleDto.bodyLens of updatedUserRole.data.toDto())
        }
      }
}

@Serializable
data class UpdateRoleRequest(
    val roles: List<Role>,
) {
  companion object {
    val bodyLens =
        createJsonBodyLens(
            serializer(),
            errorResponse = "Failed to parse user role update request",
            includeExceptionMessageInErrorResponse = true,
        )
    val example = UpdateRoleRequest(roles = UserRoleDto.example.roles)
  }
}
