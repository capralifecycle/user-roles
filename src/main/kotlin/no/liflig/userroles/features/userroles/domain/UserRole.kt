@file:UseSerializers(UuidSerializer::class)

package no.liflig.userroles.features.userroles.domain

import java.util.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import no.liflig.documentstore.entity.EntityRoot
import no.liflig.documentstore.entity.UuidEntityId
import no.liflig.userroles.common.serialization.UuidSerializer

@Serializable
data class UserRole(
    override val id: UserRoleId = UserRoleId(),
    val userId: String,
    val roles: List<Role> = emptyList(),
) : EntityRoot<UserRoleId>

@Serializable
data class Role(
    val applicationName: String? = null,
    val orgId: String? = null,
    val roleName: String,
    val roleValue: String? = null,
)

@Serializable
@JvmInline
value class UserRoleId(override val value: UUID = UUID.randomUUID()) : UuidEntityId {
  override fun toString() = value.toString()
}
