package no.liflig.userroles.roles.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import no.liflig.http4k.setup.createJsonBodyLens
import no.liflig.userroles.roles.Role
import no.liflig.userroles.roles.UserRole

@Serializable
data class UserRoleDto(
    val username: String,
    val roles: List<Role>,
) {
  companion object {
    val bodyLens = createJsonBodyLens(serializer())
    val example =
        UserRoleDto(
            username = "test.testesen",
            roles =
                listOf(
                    Role(
                        applicationName = "example-application",
                        orgId = "example-org",
                        roleName = "admin",
                        roleValue = JsonObject(emptyMap()),
                    ),
                ),
        )
  }
}

fun UserRole.toDto() = UserRoleDto(username = username, roles = roles)

@Serializable
data class ListUserRoleDto(
    val userRoles: List<UserRoleDto>,
) {
  companion object {
    val bodyLens = createJsonBodyLens(serializer())
    val example =
        ListUserRoleDto(
            userRoles = listOf(UserRoleDto.example),
        )
  }
}
