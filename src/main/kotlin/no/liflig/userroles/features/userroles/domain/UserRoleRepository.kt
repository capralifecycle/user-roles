package no.liflig.userroles.features.userroles.domain

import no.liflig.userroles.common.repository.domain.Repository
import no.liflig.userroles.features.userroles.persistence.UserRoleSearchQuery

interface UserRoleRepository : Repository<UserRole, UserRoleId> {
  suspend fun search(query: UserRoleSearchQuery): List<UserRole>
  suspend fun getByUserId(userId: String): UserRole?
}
