package no.liflig.userroles.features.userroles.api

import no.liflig.userroles.common.Api
import no.liflig.userroles.features.userroles.UserRoleRepository

class UserRoleApi(userRoleRepo: UserRoleRepository) : Api {
  override val basePath = "/userroles"

  override val endpoints =
      listOf(
          GetUserRoleEndpoint(userRoleRepo),
          DeleteUserRoleEndpoint(userRoleRepo),
          UpdateUserRoleEndpoint(userRoleRepo),
          ListUserRolesEndpoint(userRoleRepo),
      )
}
