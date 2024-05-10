package no.liflig.userroles.features.userroles.persistence

import no.liflig.documentstore.dao.AbstractSearchDao
import no.liflig.documentstore.entity.EntityList
import no.liflig.userroles.common.serialization.userRolesSerializationAdapter
import no.liflig.userroles.features.userroles.domain.UserRole
import no.liflig.userroles.features.userroles.domain.UserRoleId
import org.jdbi.v3.core.Jdbi

sealed class UserRoleSearchQuery {
  data object All : UserRoleSearchQuery()

  data class ByUserId(val userId: String) : UserRoleSearchQuery()

  data class ByOrgIdOrRoleName(val orgId: String?, val roleName: String?) : UserRoleSearchQuery()
}

class UserRoleSearchDao(
    jdbi: Jdbi,
    sqlTableName: String,
) :
    AbstractSearchDao<
        UserRoleId,
        UserRole,
        UserRoleSearchQuery,
    >(jdbi, sqlTableName, userRolesSerializationAdapter) {
  override fun search(query: UserRoleSearchQuery): EntityList<UserRole> {
    return when (query) {
      is UserRoleSearchQuery.All -> getByPredicate()
      is UserRoleSearchQuery.ByUserId -> {
        getByPredicate("data->>'userId' = :userId") { bind("userId", query.userId) }
      }
      is UserRoleSearchQuery.ByOrgIdOrRoleName -> {
        getByPredicate(
            """
              (:orgId IS NOT NULL AND :roleName IS NOT NULL AND data->'roles' @> ('[{"orgId": "' || :orgId || '", "roleName": "' || :roleName || '"}]')::jsonb)
              OR
              (:orgId IS NULL AND :roleName IS NOT NULL AND data->'roles' @> ('[{"roleName": "' || :roleName || '"}]')::jsonb)
              OR
              (:orgId IS NOT NULL AND :roleName IS NULL AND data->'roles' @> ('[{"orgId": "' || :orgId || '"}]')::jsonb)
              OR
              (:orgId IS NULL AND :roleName IS NULL)
            """
                .trimIndent(),
        ) {
          bind("orgId", query.orgId)
          bind("roleName", query.roleName)
        }
      }
    }
  }
}
