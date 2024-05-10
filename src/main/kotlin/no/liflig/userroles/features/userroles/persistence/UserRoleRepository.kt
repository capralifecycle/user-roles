package no.liflig.userroles.features.userroles.persistence

import mu.KotlinLogging
import no.liflig.documentstore.dao.CrudDao
import no.liflig.documentstore.entity.EntityList
import no.liflig.documentstore.entity.VersionedEntity
import no.liflig.userroles.features.userroles.domain.UserRole
import no.liflig.userroles.features.userroles.domain.UserRoleId

private val log = KotlinLogging.logger {}

typealias UserRoleCrud = CrudDao<UserRoleId, UserRole>

class UserRoleRepository(
    private val crudDao: CrudDao<UserRoleId, UserRole>,
    private val searchDao: UserRoleSearchDao,
) : UserRoleCrud by crudDao {
  fun getByOrgIdOrRoleName(orgId: String? = null, roleName: String? = null): EntityList<UserRole> {
    return searchDao.search(
        UserRoleSearchQuery.ByOrgIdOrRoleName(orgId = orgId, roleName = roleName))
  }

  fun getByUserId(userId: String): VersionedEntity<UserRole>? {
    val results = searchDao.search(UserRoleSearchQuery.ByUserId(userId))

    if (results.size > 1) {
      log.warn {
        "Multiple users found for user id: $userId. Picking first: " + "${results.first()}"
      }
      // throw IllegalStateException("Should never return more than 1 result.") // TODO enable this
      // when account names are available in frontend
    }

    return results.firstOrNull()
  }

  fun listAll(): EntityList<UserRole> {
    return searchDao.search(UserRoleSearchQuery.All)
  }
}
