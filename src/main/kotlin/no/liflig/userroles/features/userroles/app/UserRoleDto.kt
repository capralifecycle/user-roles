package no.liflig.userroles.features.userroles.app

import kotlinx.serialization.Serializable
import no.liflig.userroles.common.config.http4k.createBodyLens
import no.liflig.userroles.features.userroles.domain.Role
import no.liflig.userroles.features.userroles.domain.UserRole

@Serializable
data class UserRoleDto(
    val userId: String,
    val roles: List<Role>,
) {
  companion object {
    val bodyLens = createBodyLens(serializer())
    val example =
        UserRoleDto(
            userId = "ola.nordmann",
            roles =
                listOf(
                    Role(
                        applicationName = "logistics",
                        orgId = null,
                        roleName = "admin",
                        roleValue = null,
                    ),
                    Role(
                        applicationName = "admin",
                        orgId = null,
                        roleName = "view",
                        roleValue = null,
                    ),
                ))
  }
}

fun UserRole.toDto() = UserRoleDto(userId = userId, roles = roles)

@Serializable
data class ListUserRoleDto(
    val userRoles: List<UserRoleDto>,
) {
  companion object {
    val bodyLens = createBodyLens(serializer())
    val example =
        ListUserRoleDto(
            userRoles =
                listOf(
                    UserRoleDto(
                        userId = "customerName",
                        roles = listOf(exampleRole),
                    ),
                ),
        )
  }
}

val exampleRole =
    Role(
        applicationName = null,
        orgId = null,
        roleName = "admin",
        roleValue = """{"boards": [1,2,3]}""",
    )
