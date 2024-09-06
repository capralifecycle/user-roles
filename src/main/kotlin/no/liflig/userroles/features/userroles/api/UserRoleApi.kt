package no.liflig.userroles.features.userroles.api

import no.liflig.userroles.common.Api
import no.liflig.userroles.features.userroles.UserRoleRepository

class UserRoleApi(userRolesRepository: UserRoleRepository) : Api {
  override val basePath = "/userroles"

  override val endpoints =
      listOf(
          GetUserRoleEndpoint(userRolesRepository),
          DeleteUserRoleEndpoint(userRolesRepository),
          UpdateUserRoleEndpoint(userRolesRepository),
          ListUserRolesEndpoint(userRolesRepository),
      )
}
