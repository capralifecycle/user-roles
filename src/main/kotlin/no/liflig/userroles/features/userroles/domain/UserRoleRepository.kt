package no.liflig.userroles.features.userroles.domain

import no.liflig.userroles.common.repository.domain.Repository
import no.liflig.userroles.features.userroles.persistence.UserRoleSearchQuery

interface UserRoleRepository : Repository<UserRole, UserRoleId> {
  fun search(query: UserRoleSearchQuery): List<UserRole>
  fun getByUserId(userId: String): UserRole?
}
