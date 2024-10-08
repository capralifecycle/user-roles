package no.liflig.userroles.features.userroles.api

import no.liflig.userroles.common.http4k.Endpoint
import no.liflig.userroles.features.userroles.UserRoleRepository
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
class ListUserRolesEndpoint(
    private val userRoleRepo: UserRoleRepository,
) : Endpoint {
  companion object {
    val orgIdQuery = Query.string().optional("orgId")
    val roleNameQuery = Query.string().optional("roleName")
  }

  override fun route(): ContractRoute {
    val path = UserRoleApi.PATH
    val spec =
        path.meta {
          summary = "Get user roles"
          description = "Get user roles"
          queries += orgIdQuery
          queries += roleNameQuery
          returning(status = Status.OK, body = ListUserRoleDto.bodyLens to ListUserRoleDto.example)
        }
    return spec.bindContract(Method.GET) to ::handler
  }

  private fun handler(request: Request): Response {
    val orgId = orgIdQuery(request)
    val roleName = roleNameQuery(request)

    val userRoles =
        userRoleRepo.getByOrgIdOrRoleName(orgId = orgId, roleName = roleName).map {
          it.item.toDto()
        }

    return Response(Status.OK)
        .with(ListUserRoleDto.bodyLens of ListUserRoleDto(userRoles = userRoles))
  }
}
