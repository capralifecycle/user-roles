package no.liflig.userroles.administration.api

import java.time.Instant
import kotlinx.serialization.Serializable
import no.liflig.http4k.setup.createJsonBodyLens
import no.liflig.http4k.setup.errorResponse
import no.liflig.userroles.administration.UserAdministrationService
import no.liflig.userroles.administration.UserCursor
import no.liflig.userroles.administration.UserDataWithRoles
import no.liflig.userroles.administration.UserFilter
import no.liflig.userroles.administration.UserSearchField
import no.liflig.userroles.common.http4k.Endpoint
import no.liflig.userroles.roles.api.UserRoleDto
import org.http4k.contract.ContractRoute
import org.http4k.contract.meta
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.lens.Query
import org.http4k.lens.enum
import org.http4k.lens.int

class ListUsersEndpoint(
    private val userAdministrationService: UserAdministrationService,
) : Endpoint {
  companion object {
    /** Minimum 1, max 60 (limitation from Cognito). Suggestion: Default to limit 20. */
    private val limitQuery = Query.int().required("limit")
    /** See [ListUsersResponse] */
    private val cursorQuery = Query.map { UserCursor.fromString(it) }.optional("cursor")

    private val searchStringQuery = Query.optional("searchString")
    private val searchFieldQuery = Query.enum<UserSearchField>().optional("searchField")
    private val organizationIdQuery = Query.optional("orgId")
    private val applicationNameQuery = Query.optional("applicationName")
    private val roleNameQuery = Query.optional("roleName")
  }

  override fun route(): ContractRoute {
    val path = UserAdministrationApi.PATH
    val spec =
        path.meta {
          summary = "List users"
          queries +=
              listOf(
                  limitQuery,
                  cursorQuery,
                  searchStringQuery,
                  searchFieldQuery,
                  organizationIdQuery,
                  applicationNameQuery,
                  roleNameQuery,
              )
          returning(Status.OK, ListUsersResponse.bodyLens to ListUsersResponse.example)
        }
    return spec.bindContract(Method.GET) to ::handler
  }

  private fun handler(request: Request): Response {
    val limit = limitQuery(request)
    if (limit !in 1..60) {
      return errorResponse(
          request,
          Status.BAD_REQUEST,
          title = "Invalid parameter to List Users",
          detail = "Limit (page size) must be in the range [1, 60]",
      )
    }

    val filter =
        UserFilter(
            searchString = searchStringQuery(request),
            searchField = searchFieldQuery(request),
            organizationId = organizationIdQuery(request),
            applicationName = applicationNameQuery(request),
            roleName = roleNameQuery(request),
        )
    val cursor = cursorQuery(request)

    val result =
        userAdministrationService.listUsers(limit = limit, filter = filter, cursor = cursor)
    val responseBody =
        ListUsersResponse(
            nextCursor = result.nextCursor?.toString(),
            users = result.users,
        )
    return Response(Status.OK).with(ListUsersResponse.bodyLens.of(responseBody))
  }
}

@Serializable
data class ListUsersResponse(
    /** See [UserCursor]. Null if there are no more users to fetch. */
    val nextCursor: String?,
    val users: List<UserDataWithRoles>,
) {
  companion object {
    val bodyLens = createJsonBodyLens(serializer())
    val example =
        ListUsersResponse(
            nextCursor =
                UserCursor(
                        cognitoPaginationToken = "example-pagination-token",
                        pageOffset = 0,
                    )
                    .toString(),
            users =
                listOf(
                    UserDataWithRoles(
                        username = "test.testesen",
                        email = "test@example.org",
                        phoneNumber = "12345678",
                        name = "Test Testesen",
                        givenName = "Test",
                        familyName = "Testesen",
                        preferredUsername = "test",
                        userStatus = "CONFIRMED",
                        enabled = true,
                        createdAt = Instant.parse("2025-12-03T11:32:59Z"),
                        roles = UserRoleDto.example.roles,
                    )
                ),
        )
  }
}
