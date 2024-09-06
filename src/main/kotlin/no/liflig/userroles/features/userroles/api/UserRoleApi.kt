package no.liflig.userroles.features.userroles.api

import no.liflig.userroles.ServiceRegistry
import no.liflig.userroles.common.Api

class UserRoleApi(registry: ServiceRegistry) : Api {
  override val basePath = "/userroles"

  override val endpoints =
      listOf(
          GetUserRoleEndpoint(registry.userRolesRepository),
          DeleteUserRoleEndpoint(registry.userRolesRepository),
          UpdateUserRoleEndpoint(registry.userRolesRepository),
          ListUserRolesEndpoint(registry.userRolesRepository),
      )
}
