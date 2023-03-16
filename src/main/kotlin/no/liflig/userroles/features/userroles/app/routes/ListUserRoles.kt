package no.liflig.userroles.features.userroles.app.routes

import kotlinx.coroutines.runBlocking
import no.liflig.userroles.features.userroles.app.ListUserRoleDto
import no.liflig.userroles.features.userroles.app.toDto
import no.liflig.userroles.features.userroles.domain.UserRoleRepository
import no.liflig.userroles.features.userroles.persistence.UserRoleSearchQuery
import org.http4k.contract.ContractRoute
import org.http4k.contract.RouteMetaDsl
import org.http4k.contract.div
import org.http4k.contract.meta
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.lens.Query
import org.http4k.lens.string

/**
 * Contains the endpoint for getting user roles
 * */
class ListUserRoles(
  private val path: String,
  private val userRoleRepository: UserRoleRepository,
) {
  // private val orgIdQuery = Query.map { PersonId(UUID.fromString(it)) }.optional("orgId")
  private val orgIdQuery = Query.string().optional("orgId")
  private val roleNameQuery = Query.string().optional("roleName")

  private fun meta(): RouteMetaDsl.() -> Unit = {
    summary = "Get user roles"
    description = "Get user roles"
    queries += orgIdQuery
    queries += roleNameQuery
    returning(status = Status.OK, body = ListUserRoleDto.bodyLens to ListUserRoleDto.example)
  }

  fun route(): ContractRoute {
    return path meta meta() bindContract Method.GET to
      { req: Request ->

        val orgId = orgIdQuery(req)
        val roleName = roleNameQuery(req)

        runBlocking {
          val userRoles = userRoleRepository.search(
            UserRoleSearchQuery(
              orgId = orgId,
              roleName = roleName,
            ),
          ).map { it.toDto() }

          Response(Status.OK).with(ListUserRoleDto.bodyLens of ListUserRoleDto(items = userRoles))
        }
      }
  }
}
