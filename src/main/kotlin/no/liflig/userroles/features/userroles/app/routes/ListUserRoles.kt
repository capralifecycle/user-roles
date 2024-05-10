package no.liflig.userroles.features.userroles.app.routes

import no.liflig.userroles.common.Endpoint
import no.liflig.userroles.features.userroles.app.ListUserRoleDto
import no.liflig.userroles.features.userroles.app.toDto
import no.liflig.userroles.features.userroles.persistence.UserRoleRepository
import org.http4k.contract.ContractRoute
import org.http4k.contract.meta
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.lens.Query
import org.http4k.lens.string

/** Contains the endpoint for getting user roles */
class ListUserRoles(
    private val userRoleRepository: UserRoleRepository,
) : Endpoint {
  // private val orgIdQuery = Query.map { PersonId(UUID.fromString(it)) }.optional("orgId")
  private val orgIdQuery = Query.string().optional("orgId")
  private val roleNameQuery = Query.string().optional("roleName")

  override fun route(basePath: String): ContractRoute {
    val path = basePath
    val spec =
        path meta
            {
              summary = "Get user roles"
              description = "Get user roles"
              queries += orgIdQuery
              queries += roleNameQuery
              returning(
                  status = Status.OK, body = ListUserRoleDto.bodyLens to ListUserRoleDto.example)
            }
    return spec bindContract Method.GET to ::handler
  }

  private fun handler(request: Request): Response {
    val orgId = orgIdQuery(request)
    val roleName = roleNameQuery(request)

    val userRoles =
        userRoleRepository.getByOrgIdOrRoleName(orgId = orgId, roleName = roleName).map {
          it.toDto()
        }

    return Response(Status.OK)
        .with(ListUserRoleDto.bodyLens of ListUserRoleDto(userRoles = userRoles))
  }
}
