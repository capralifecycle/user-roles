package no.liflig.userroles.roles

import java.util.UUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import no.liflig.documentstore.entity.Entity
import no.liflig.documentstore.entity.UuidEntityId
import no.liflig.userroles.common.serialization.SerializableUUID

@Serializable
data class UserRole(
    override val id: UserRoleId = UserRoleId(),
    // TODO: Rename and migrate to username, for consistency with admin module
    val userId: String,
    val roles: List<Role> = emptyList(),
) : Entity<UserRoleId> {
  fun isSuperAdmin(): Boolean = roles.any { it.roleName == SUPER_ADMIN_ROLE_NAME }
}

@Serializable
data class Role(
    /** The application that this role grants access to. */
    val applicationName: String? = null,
    /** The organization that this role grants access to. */
    val orgId: String? = null,
    /** The user's role in this organization and application. */
    val roleName: String,
    /** Arbitrary JSON data to attach to the role. */
    val roleValue: JsonElement? = null,
)

@Serializable
@JvmInline
value class UserRoleId(override val value: SerializableUUID = UUID.randomUUID()) : UuidEntityId {
  override fun toString() = value.toString()
}

/**
 * Special value for [Role.roleName], which is included when listing users regardless of filters on
 * orgId / applicationName.
 */
const val SUPER_ADMIN_ROLE_NAME = "SUPER_ADMIN"
