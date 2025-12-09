package no.liflig.userroles.administration.api

import no.liflig.http4k.setup.createJsonBodyLens
import no.liflig.userroles.administration.UpdateUserRequest
import no.liflig.userroles.administration.UserAdministrationService
import no.liflig.userroles.administration.UserAdministrationService.Companion.IDENTITY_PROVIDER_NAME
import no.liflig.userroles.common.http4k.Endpoint
import org.http4k.contract.ContractRoute
import org.http4k.contract.meta
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status

class UpdateUserEndpoint(
    private val userAdministrationService: UserAdministrationService,
) : Endpoint {
  override fun route(): ContractRoute {
    val path = UserAdministrationApi.PATH
    val spec =
        path.meta {
          summary =
              "Update existing user in identity provider (${IDENTITY_PROVIDER_NAME}), and their associated roles"
          operationId = "updateUser"
          receiving(requestBodyLens to requestBodyExample)
          /** No response body - see [UserAdministrationService.updateUser] for why. */
          returning(Status.OK)
        }
    return spec.bindContract(Method.PUT) to ::handler
  }

  private fun handler(request: Request): Response {
    val requestBody = requestBodyLens(request)

    userAdministrationService.updateUser(requestBody)

    return Response(Status.OK)
  }

  companion object {
    private val requestBodyLens = createJsonBodyLens(UpdateUserRequest.serializer())
    private val requestBodyExample = UpdateUserRequest(user = EXAMPLE_USER_UPDATE_DATA)
  }
}
