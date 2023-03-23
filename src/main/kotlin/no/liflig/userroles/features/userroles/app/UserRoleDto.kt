package no.liflig.userroles.features.userroles.app

import java.util.*
import kotlinx.serialization.Serializable
import no.liflig.documentstore.entity.Version
import no.liflig.userroles.common.config.http4k.createBodyLens
import no.liflig.userroles.features.userroles.domain.Role
import no.liflig.userroles.features.userroles.domain.RoleName
import no.liflig.userroles.features.userroles.domain.UserRole
import no.liflig.userroles.features.userroles.domain.UserRoleId

@kotlinx.serialization.Serializable
data class UserRoleDto(
    val id: String,
    val userId: String,
    val userRoles: List<RoleDto>,
) {
  companion object {
    val bodyLens = createBodyLens(serializer())
    val example =
        UserRoleDto(
            id = "id",
            userId = "customerName",
            userRoles = listOf(RoleDto.example),
        )
  }
}

fun UserRole.toDto() =
    UserRoleDto(
        id = id.id.toString(),
        userId = userId,
        userRoles = userRoles.map { it.toDto() },
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
    val roleName: RoleName,
    val roleValue: String? = null,
) {
  companion object {
    val bodyLens = createBodyLens(serializer())
    val example =
        RoleDto(
            orgId = "id",
            roleName = RoleName.ADMIN,
            roleValue = """{"boards": [1,2,3]}""",
        )
  }
}

fun UserRoleDto.toDomain() =
    UserRole(
        id = UserRoleId(UUID.fromString(id)),
        version = Version.initial(),
        userId = userId,
        userRoles = userRoles.map { it.toDomain() },
    )

fun RoleDto.toDomain() =
    Role(
        orgId = orgId,
        roleName = roleName,
        roleValue = roleValue,
    )

@kotlinx.serialization.Serializable
data class ListUserRoleDto(
    val items: List<UserRoleDto>,
) {
  companion object {
    val bodyLens = createBodyLens(serializer())
    val example =
        ListUserRoleDto(
            items =
                listOf(
                    UserRoleDto(
                        id = "id",
                        userId = "customerName",
                        userRoles = listOf(RoleDto.example),
                    ),
                ),
        )
  }
}
