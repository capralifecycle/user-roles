package no.liflig.userroles.roles

import java.util.UUID
import kotlinx.serialization.Serializable
import no.liflig.documentstore.entity.Entity
import no.liflig.documentstore.entity.UuidEntityId
import no.liflig.userroles.common.serialization.SerializableUUID

@Serializable
data class UserRole(
    override val id: UserRoleId = UserRoleId(),
    val userId: String,
    val roles: List<Role> = emptyList(),
) : Entity<UserRoleId>

@Serializable
data class Role(
    val applicationName: String? = null,
    val orgId: String? = null,
    val roleName: String,
    val roleValue: String? = null,
)

@Serializable
@JvmInline
value class UserRoleId(override val value: SerializableUUID = UUID.randomUUID()) : UuidEntityId {
  override fun toString() = value.toString()
}
