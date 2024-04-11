package no.liflig.userroles.features.userroles.persistence

import no.liflig.documentstore.dao.AbstractSearchDao
import no.liflig.documentstore.entity.VersionedEntity
import no.liflig.userroles.common.serialization.userRolesSerializationAdapter
import no.liflig.userroles.features.userroles.domain.UserRole
import no.liflig.userroles.features.userroles.domain.UserRoleId
import org.jdbi.v3.core.Jdbi

data class UserRoleSearchQuery(
    val userId: String? = null,
    val orgId: String? = null,
    val roleName: String? = null,
)

class UserRoleSearchDao(
    jdbi: Jdbi,
    sqlTableName: String,
) :
    AbstractSearchDao<
        UserRoleId,
        UserRole,
        UserRoleSearchQuery,
    >(jdbi, sqlTableName, userRolesSerializationAdapter) {
  override fun search(query: UserRoleSearchQuery): List<VersionedEntity<UserRole>> =
      getByPredicate(
          """
            (:userId IS NULL OR data->>'userId' = :userId)
            AND
            (
              (:orgId IS NOT NULL AND :roleName IS NOT NULL AND data->'roles' @> ('[{"orgId": "' || :orgId || '", "roleName": "' || :roleName || '"}]')::jsonb)
              OR
              (:orgId IS NULL AND :roleName IS NOT NULL AND data->'roles' @> ('[{"roleName": "' || :roleName || '"}]')::jsonb)
              OR
              (:orgId IS NOT NULL AND :roleName IS NULL AND data->'roles' @> ('[{"orgId": "' || :orgId || '"}]')::jsonb)
              OR
              (:orgId IS NULL AND :roleName IS NULL)
            )
          """
              .trimIndent(),
      ) {
        bind("userId", query.userId)
        bind("orgId", query.orgId)
        bind("roleName", query.roleName)
      }

  // (data->'userRoles' @> ('[{"orgId": "' || :orgId || '", "roleName": "' || :roleName ||
  // '"}]')::jsonb)
  fun listAll(): List<VersionedEntity<UserRole>> {
    return getByPredicate().map { it }
  }
}
