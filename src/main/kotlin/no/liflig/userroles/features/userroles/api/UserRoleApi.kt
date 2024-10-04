package no.liflig.userroles.features.userroles.api

import no.liflig.userroles.common.http4k.EndpointGroup
import no.liflig.userroles.features.userroles.UserRoleRepository

class UserRoleApi(userRoleRepo: UserRoleRepository) : EndpointGroup {
  override val endpoints =
      listOf(
          GetUserRoleEndpoint(userRoleRepo),
          DeleteUserRoleEndpoint(userRoleRepo),
          UpdateUserRoleEndpoint(userRoleRepo),
          ListUserRolesEndpoint(userRoleRepo),
      )

  companion object {
    const val PATH = "/userroles"
  }
}
