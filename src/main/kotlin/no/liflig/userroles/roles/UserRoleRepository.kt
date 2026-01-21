package no.liflig.userroles.roles

import no.liflig.documentstore.entity.Versioned
import no.liflig.documentstore.repository.RepositoryJdbi
import no.liflig.userroles.common.serialization.KotlinSerialization
import org.jdbi.v3.core.Jdbi

class UserRoleRepository(jdbi: Jdbi) :
    RepositoryJdbi<UserRoleId, UserRole>(
        jdbi,
        tableName = TABLE_NAME,
        serializationAdapter = KotlinSerialization(UserRole.serializer()),
    ) {
  companion object {
    const val TABLE_NAME = "userroles"
    const val USERNAME_UNIQUE_INDEX_NAME = "user_role_username_idx"
  }

  fun getByUsername(username: String): Versioned<UserRole>? {
    return getByPredicate("data->>'username' = :username") { bind("username", username) }
        .firstOrNull()
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

  fun listByUsernames(usernames: List<String>): List<Versioned<UserRole>> {
    return getByPredicate(
        """
        data->>'username' = ANY(:usernames)
        """
            .trimIndent(),
    ) {
      bindArray("usernames", String::class.java, usernames)
    }
  }
}
