package no.liflig.userroles.features.userroles.persistence

import mu.KotlinLogging
import no.liflig.documentstore.dao.CrudDao
import no.liflig.userroles.features.userroles.domain.UserRole
import no.liflig.userroles.features.userroles.domain.UserRoleId

private val log = KotlinLogging.logger {}

typealias UserRoleCrud = CrudDao<UserRoleId, UserRole>

class UserRoleRepository(
    private val crudDao: CrudDao<UserRoleId, UserRole>,
    private val searchDao: UserRoleSearchDao,
) : UserRoleCrud by crudDao {
  fun getByOrgIdOrRoleName(orgId: String? = null, roleName: String? = null): List<UserRole> {
    return searchDao
        .search(UserRoleSearchQuery.ByOrgIdOrRoleName(orgId = orgId, roleName = roleName))
        .map { it.item.version(it.version) }
  }

  fun getByUserId(userId: String): UserRole? {
    val results =
        searchDao.search(UserRoleSearchQuery.ByUserId(userId)).map { it.item.version(it.version) }

    if (results.size > 1) {
      log.warn {
        "Multiple users found for user id: $userId. Picking first: " + "${results.first()}"
      }
      // throw IllegalStateException("Should never return more than 1 result.") // TODO enable this
      // when account names are available in frontend
    }

    return results.firstOrNull()
  }

  fun listAll(): List<UserRole> {
    return searchDao.search(UserRoleSearchQuery.All).map { it.item.version(it.version) }
  }
}
