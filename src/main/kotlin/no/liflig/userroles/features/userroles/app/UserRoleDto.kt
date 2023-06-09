package no.liflig.userroles.features.userroles.app

import java.util.*
import kotlinx.serialization.Serializable
import no.liflig.documentstore.entity.Version
import no.liflig.userroles.common.config.http4k.createBodyLens
import no.liflig.userroles.features.userroles.domain.Role
import no.liflig.userroles.features.userroles.domain.UserRole
import no.liflig.userroles.features.userroles.domain.UserRoleId

@kotlinx.serialization.Serializable
data class UserRoleDto(
    val id: String,
    val userId: String,
    val roles: List<RoleDto>,
) {
  companion object {
    val bodyLens = createBodyLens(serializer())
    val example =
        UserRoleDto(
            id = "id",
            userId = "customerName",
            roles = listOf(RoleDto.example),
        )
  }
}

fun UserRole.toDto() =
    UserRoleDto(
        id = id.id.toString(),
        userId = userId,
        roles = roles.map { it.toDto() },
    )

fun Role.toDto() =
    RoleDto(
        orgId = orgId,
        roleName = roleName,
        roleValue = roleValue,
    )

@Serializable
data class RoleDto(
    val orgId: String? = null,
    val roleName: String,
    val roleValue: String? = null,
) {
  companion object {
    val bodyLens = createBodyLens(serializer())
    val example =
        RoleDto(
            orgId = "id",
            roleName = "admin",
            roleValue = """{"boards": [1,2,3]}""",
        )
  }
}

fun UserRoleDto.toDomain() =
    UserRole(
        id = UserRoleId(UUID.fromString(id)),
        version = Version.initial(),
        userId = userId,
        roles = roles.map { it.toDomain() },
    )

fun RoleDto.toDomain() =
    Role(
        orgId = orgId,
        roleName = roleName,
        roleValue = roleValue,
    )

@kotlinx.serialization.Serializable
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
                        id = "id",
                        userId = "customerName",
                        roles = listOf(RoleDto.example),
                    ),
                ),
        )
  }
}
