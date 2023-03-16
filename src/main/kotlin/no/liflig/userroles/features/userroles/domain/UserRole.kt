package no.liflig.userroles.features.userroles.domain

import kotlinx.serialization.Serializable
import no.liflig.documentstore.entity.EntityRoot
import no.liflig.documentstore.entity.UuidEntityId
import no.liflig.documentstore.entity.Version
import no.liflig.userroles.common.serialization.LongSerializer
import no.liflig.userroles.common.serialization.UuidEntityIdSerializer
import java.util.*

@Serializable
data class UserRole(
  override val id: UserRoleId,
  @Serializable(with = VersionSerializer::class)
  val version: Version,
  val userId: String,
  val userRoles: List<Role>,
) : EntityRoot<UserRoleId> {
  companion object {
    fun create(
      id: UserRoleId = UserRoleId.create(),
      userId: String,
      roles: List<Role> = emptyList(),
    ) = UserRole(
      id = id,
      version = Version.initial(),
      userId = userId,
      userRoles = roles,
    )
  }

  fun changeRoles(roles: List<Role>) = update(
    userRoles = roles,
  )

  fun version(version: Version) = update(
    version = version,
  )

  fun update(
    userRoles: List<Role> = this.userRoles,
    version: Version = this.version,
  ): UserRole = UserRole(
    id = id,
    version = version,
    userId = userId,
    userRoles = userRoles,
  )
}

@Serializable
data class Role(
  val orgId: String? = null,
  val roleName: RoleName,
  val roleValue: String? = null,
)

@Serializable(with = UserRoleIdSerializer::class)
data class UserRoleId(
  override val id: UUID,
) : UuidEntityId {

  companion object {
    fun create(id: UUID = UUID.randomUUID()) = UserRoleId(
      id = id,
    )
  }
}

object UserRoleIdSerializer : UuidEntityIdSerializer<UserRoleId>(::UserRoleId)
object VersionSerializer : LongSerializer(::Version)
