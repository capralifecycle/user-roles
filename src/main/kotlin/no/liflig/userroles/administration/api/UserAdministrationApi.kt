package no.liflig.userroles.administration.api

import no.liflig.userroles.administration.UserAdministrationService
import no.liflig.userroles.common.http4k.EndpointGroup

class UserAdministrationApi(userAdministrationService: UserAdministrationService) : EndpointGroup {
  override val endpoints =
      listOf(
          ListUsersEndpoint(userAdministrationService),
          DeleteUserEndpoint(userAdministrationService),
      )

  companion object {
    const val PATH = "/administration/users"
  }
}
