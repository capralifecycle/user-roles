package no.liflig.userroles.features.userroles.app

import no.liflig.userroles.common.Api
import no.liflig.userroles.features.userroles.app.routes.DeleteUserRole
import no.liflig.userroles.features.userroles.app.routes.GetUserRole
import no.liflig.userroles.features.userroles.app.routes.ListUserRoles
import no.liflig.userroles.features.userroles.app.routes.UpdateUserRole
import no.liflig.userroles.features.userroles.persistence.UserRoleRepository

class UserRoleApi(userRolesRepository: UserRoleRepository) : Api {
  override val basePath = "/userroles"

  override val endpoints =
      listOf(
          GetUserRole(userRolesRepository),
          DeleteUserRole(userRolesRepository),
          UpdateUserRole(userRolesRepository),
          ListUserRoles(userRolesRepository),
      )
}
