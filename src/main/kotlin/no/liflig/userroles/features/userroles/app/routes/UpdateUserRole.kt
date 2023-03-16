package no.liflig.userroles.features.userroles.app.routes

import kotlinx.coroutines.runBlocking
import no.liflig.userroles.common.config.http4k.createBodyLens
import no.liflig.userroles.common.config.http4k.userIdPathLens
import no.liflig.userroles.features.userroles.app.RoleDto
import no.liflig.userroles.features.userroles.app.UserRoleDto
import no.liflig.userroles.features.userroles.app.toDomain
import no.liflig.userroles.features.userroles.app.toDto
import no.liflig.userroles.features.userroles.domain.UserRole
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

/**
 * Contains the endpoint for updating a user role
 * */
class UpdateUserRole(
  private val path: String,
  private val userRoleRepository: UserRoleRepository,
) {

  @kotlinx.serialization.Serializable
  data class UpdateRoleRequest(
    val roles: List<RoleDto>,
  ) {
    companion object {
      val bodyLens = createBodyLens(serializer())
      val example = UpdateRoleRequest(
        roles = listOf(RoleDto.example),
      )
    }
  }

  private fun meta(): RouteMetaDsl.() -> Unit = {
    summary = "Update user role"
    description = "Update user role"
    receiving(body = UpdateRoleRequest.bodyLens to UpdateRoleRequest.example)
    returning(Status.OK, body = UserRoleDto.bodyLens to UserRoleDto.example)
  }

  fun route(): ContractRoute {
    return path / userIdPathLens meta meta() bindContract Method.PUT to { id ->
      { request: Request ->

        val body = UpdateRoleRequest.bodyLens(request)

        runBlocking {
          val userRole = userRoleRepository.getByUserId(id)
          if (userRole == null) {
            val createdUserRole = userRoleRepository.create(
              UserRole.create(
                userId = id,
                roles = body.roles.map { it.toDomain() },
              ),
            )
            Response(Status.OK).with(UserRoleDto.bodyLens of createdUserRole.toDto())
          } else {
            var updatedUserRole = userRole

            updatedUserRole = updatedUserRole.changeRoles(body.roles.map { it.toDomain() })
            val f = userRoleRepository.update(updatedUserRole)

            Response(Status.OK).with(UserRoleDto.bodyLens of f.toDto())
          }
        }
      }
    }
  }
}
