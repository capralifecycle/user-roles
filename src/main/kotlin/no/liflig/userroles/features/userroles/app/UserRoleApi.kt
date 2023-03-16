package no.liflig.userroles.features.userroles.app

import no.liflig.userroles.features.userroles.app.routes.DeleteUserRole
import no.liflig.userroles.features.userroles.app.routes.GetUserRole
import no.liflig.userroles.features.userroles.app.routes.ListUserRoles
import no.liflig.userroles.features.userroles.app.routes.UpdateUserRole
import no.liflig.userroles.features.userroles.domain.UserRoleRepository

class UserRoleApi(basePath: String, userRolesRepository: UserRoleRepository) {
  val routes = listOf(
    GetUserRole(basePath, userRolesRepository).route(),
    DeleteUserRole(basePath, userRolesRepository).route(),
    UpdateUserRole(basePath, userRolesRepository).route(),
    ListUserRoles(basePath, userRolesRepository).route(),
  )
}
