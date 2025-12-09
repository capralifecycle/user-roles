package no.liflig.userroles.administration.api

import kotlinx.serialization.Serializable
import no.liflig.http4k.setup.createJsonBodyLens
import no.liflig.publicexception.ErrorCode
import no.liflig.publicexception.PublicException
import no.liflig.userroles.administration.UserAdministrationService
import no.liflig.userroles.administration.UserAdministrationService.Companion.IDENTITY_PROVIDER_NAME
import no.liflig.userroles.administration.UserCursor
import no.liflig.userroles.administration.UserDataWithRoles
import no.liflig.userroles.administration.UserFilter
import no.liflig.userroles.administration.UserSearchField
import no.liflig.userroles.common.http4k.Endpoint
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
  override fun route(): ContractRoute {
    val path = UserAdministrationApi.PATH
    val spec =
        path.meta {
          summary =
              "List users from identity provider (${IDENTITY_PROVIDER_NAME}), along with their associated roles"
          operationId = "listUsers"
          queries +=
              listOf(
                  limitQuery,
                  cursorQuery,
                  searchStringQuery,
                  searchFieldQuery,
                  orgIdQuery,
                  applicationNameQuery,
                  roleNameQuery,
              )
          returning(Status.OK, ListUsersResponse.bodyLens to ListUsersResponse.example)
        }
    return spec.bindContract(Method.GET) to ::handler
  }

  private fun handler(request: Request): Response {
    val limit = getLimitFromRequest(request)
    val filter = getUserFilterFromRequest(request)
    val cursor = getCursorFromRequest(request, limit = limit)

    val result = userAdministrationService.listUsers(limit, filter, cursor)

    val responseBody =
        ListUsersResponse(
            nextCursor = result.nextCursor?.toString(),
            users = result.users,
        )
    return Response(Status.OK).with(ListUsersResponse.bodyLens.of(responseBody))
  }

  companion object {
    /** Minimum 1, max 60 (limitation from Cognito). Suggestion: Default to limit 20. */
    private val limitQuery = Query.int().required("limit")
    /** See [ListUsersResponse] */
    private val cursorQuery = Query.optional("cursor")

    private val searchStringQuery = Query.optional("searchString")
    private val searchFieldQuery = Query.enum<UserSearchField>().optional("searchField")
    private val orgIdQuery = Query.optional("orgId")
    private val applicationNameQuery = Query.optional("applicationName")
    private val roleNameQuery = Query.optional("roleName")

    fun getLimitFromRequest(request: Request): Int {
      val limit = limitQuery(request)
      if (limit !in 1..60) {
        throw PublicException(
            ErrorCode.BAD_REQUEST,
            publicMessage = "Invalid limit parameter given to List Users",
            publicDetail = "Limit (page size) must be in the range [1, 60]",
        )
      }
      return limit
    }

    fun getUserFilterFromRequest(request: Request): UserFilter {
      return UserFilter(
          searchString = searchStringQuery(request),
          searchField = searchFieldQuery(request),
          orgId = orgIdQuery(request),
          applicationName = applicationNameQuery(request),
          roleName = roleNameQuery(request),
      )
    }

    fun getCursorFromRequest(request: Request, limit: Int): UserCursor? {
      return cursorQuery(request)?.let { UserCursor.fromString(it, limit = limit) }
    }
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
            users = listOf(EXAMPLE_USER_DATA_WITH_ROLES),
        )
  }
}
