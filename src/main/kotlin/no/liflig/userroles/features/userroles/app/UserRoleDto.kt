package no.liflig.userroles.features.userroles.app

import kotlinx.serialization.Serializable
import no.liflig.userroles.common.config.http4k.createBodyLens
import no.liflig.userroles.features.userroles.domain.Role
import no.liflig.userroles.features.userroles.domain.UserRole

@Serializable
data class UserRoleDto(
    val userId: String,
    val roles: List<RoleDto>,
) {
  companion object {
    val bodyLens = createBodyLens(serializer())
    val example =
        UserRoleDto(
            userId = "ola.nordmann",
            roles =
                listOf(
                    RoleDto(
                        applicationName = "logistics",
                        orgId = null,
                        roleName = "admin",
                        roleValue = null,
                    ),
                    RoleDto(
                        applicationName = "admin",
                        orgId = null,
                        roleName = "view",
                        roleValue = null,
                    ),
                ))
  }
}

fun UserRole.toDto() =
    UserRoleDto(
        userId = userId,
        roles = roles.map { it.toDto() },
    )

fun Role.toDto() =
    RoleDto(
        applicationName = applicationName,
        orgId = orgId,
        roleName = roleName,
        roleValue = roleValue,
    )

@Serializable
data class RoleDto(
    val applicationName: String? = null,
    val orgId: String? = null,
    val roleName: String,
    val roleValue: String? = null,
) {
  companion object {
    val bodyLens = createBodyLens(serializer())
    val example =
        RoleDto(
            applicationName = null,
            orgId = null,
            roleName = "admin",
            roleValue = """{"boards": [1,2,3]}""",
        )
  }
}

fun RoleDto.toDomain() =
    Role(
        applicationName = applicationName,
        orgId = orgId,
        roleName = roleName,
        roleValue = roleValue,
    )

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
                        roles = listOf(RoleDto.example),
                    ),
                ),
        )
  }
}
