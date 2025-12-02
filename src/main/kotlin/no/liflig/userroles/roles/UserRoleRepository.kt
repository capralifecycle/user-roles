package no.liflig.userroles.roles

import no.liflig.documentstore.entity.Versioned
import no.liflig.documentstore.repository.RepositoryJdbi
import no.liflig.userroles.common.serialization.KotlinSerialization
import org.jdbi.v3.core.Jdbi

class UserRoleRepository(jdbi: Jdbi) :
    RepositoryJdbi<UserRoleId, UserRole>(
        jdbi,
        tableName = "userroles",
        serializationAdapter = KotlinSerialization(UserRole.serializer()),
    ) {
  fun getByUserId(userId: String): Versioned<UserRole>? {
    return getByPredicate("data->>'userId' = :userId") { bind("userId", userId) }.firstOrNull()
  }

  fun getByOrgIdOrRoleName(
      orgId: String? = null,
      roleName: String? = null,
  ): List<Versioned<UserRole>> {
    return getByPredicate(
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
      bind("orgId", orgId)
      bind("roleName", roleName)
    }
  }
}
