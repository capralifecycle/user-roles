package no.liflig.userroles.administration.api

import no.liflig.userroles.administration.UserAdministrationService
import no.liflig.userroles.common.http4k.EndpointGroup

class UserAdministrationApi(userAdministrationService: UserAdministrationService) : EndpointGroup {
  override val endpoints =
      listOf(
          ListUsersEndpoint(userAdministrationService),
      )

  companion object {
    const val PATH = "/users"
  }
}
