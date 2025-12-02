package no.liflig.userroles.roles.api

import kotlinx.serialization.Serializable
import no.liflig.http4k.setup.createJsonBodyLens
import no.liflig.userroles.roles.Role
import no.liflig.userroles.roles.UserRole

@Serializable
data class UserRoleDto(
    val userId: String,
    val roles: List<Role>,
) {
  companion object {
    val bodyLens = createJsonBodyLens(serializer())
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
                ),
        )
  }
}

fun UserRole.toDto() = UserRoleDto(userId = userId, roles = roles)

@Serializable
data class ListUserRoleDto(
    val userRoles: List<UserRoleDto>,
) {
  companion object {
    val bodyLens = createJsonBodyLens(serializer())
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
