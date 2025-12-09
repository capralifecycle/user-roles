package no.liflig.userroles.administration.api

import no.liflig.http4k.setup.createJsonBodyLens
import no.liflig.userroles.administration.CreateUserRequest
import no.liflig.userroles.administration.InvitationMessageType
import no.liflig.userroles.administration.UserAdministrationService
import no.liflig.userroles.administration.UserAdministrationService.Companion.IDENTITY_PROVIDER_NAME
import no.liflig.userroles.administration.UserDataWithRoles
import no.liflig.userroles.common.http4k.Endpoint
import org.http4k.contract.ContractRoute
import org.http4k.contract.meta
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with

class CreateUserEndpoint(
    private val userAdministrationService: UserAdministrationService,
) : Endpoint {
  override fun route(): ContractRoute {
    val path = UserAdministrationApi.PATH
    val spec =
        path.meta {
          summary =
              "Register new user in identity provider (${IDENTITY_PROVIDER_NAME}), and create associated roles"
          operationId = "createUser"
          receiving(requestBodyLens to requestBodyExample)
          returning(Status.OK, responseBodyLens to EXAMPLE_USER_DATA_WITH_ROLES)
        }
    return spec.bindContract(Method.POST) to ::handler
  }

  private fun handler(request: Request): Response {
    val requestBody = requestBodyLens(request)

    val createdUser = userAdministrationService.createUser(requestBody)

    return Response(Status.OK).with(responseBodyLens.of(createdUser))
  }

  companion object {
    private val requestBodyLens = createJsonBodyLens(CreateUserRequest.serializer())
    private val requestBodyExample =
        CreateUserRequest(
            user = EXAMPLE_USER_UPDATE_DATA,
            invitationMessages = setOf(InvitationMessageType.EMAIL),
        )

    private val responseBodyLens = createJsonBodyLens(UserDataWithRoles.serializer())
  }
}
