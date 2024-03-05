package no.liflig.userroles.features.userroles.persistence

import mu.KotlinLogging
import no.liflig.documentstore.dao.ConflictDaoException
import no.liflig.documentstore.dao.CrudDao
import no.liflig.userroles.common.repository.RepositoryException
import no.liflig.userroles.features.userroles.domain.UserRole
import no.liflig.userroles.features.userroles.domain.UserRoleId
import no.liflig.userroles.features.userroles.domain.UserRoleRepository

private val log = KotlinLogging.logger {}

class UserRoleRepositoryJdbi(
    private val crudDao: CrudDao<UserRoleId, UserRole>,
    private val searchRepo: UserRoleSearchRepositoryJdbi,
) : UserRoleRepository {
  override fun create(item: UserRole): UserRole {
    val o = crudDao.create(item)
    return o.item.version(o.version)
  }

  override fun get(id: UserRoleId): UserRole? {
    val o = crudDao.get(id)
    return o?.let { o.item.version(it.version) }
  }

  override fun update(item: UserRole): UserRole {
    try {
      val o = crudDao.update(item, item.version)
      return o.item.version(o.version)
    } catch (_: ConflictDaoException) {
      throw RepositoryException()
    }
  }

  override fun delete(item: UserRole) {
    crudDao.delete(item.id, item.version)
  }

  override fun search(query: UserRoleSearchQuery): List<UserRole> {
    return searchRepo.search(query).map { it.item.version(it.version) }
  }

  override fun getByUserId(userId: String): UserRole? {
    val results = search(UserRoleSearchQuery(userId = userId))

    return if (results.isEmpty()) {
      null
    } else if (results.size > 1) {
      log.warn {
        "Multiple users found for user id: $userId. Picking first: " + "${results.first()}"
      }
      return results.first()
      // throw IllegalStateException("Should never return more than 1 result.") // TODO enable this
      // when account names are available in frontend
    } else {
      results.first()
    }
  }

  override fun listAll(): List<UserRole> {
    return searchRepo.listAll().map { it.item }
  }
}
