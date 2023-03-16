package no.liflig.userroles.common.repository.domain

import no.liflig.documentstore.entity.Entity
import no.liflig.documentstore.entity.EntityId

interface Repository<T : Entity<ID>, ID : EntityId> {
  suspend fun create(item: T): T
  suspend fun get(id: ID): T?
  suspend fun update(item: T): T
  suspend fun delete(item: T)
  suspend fun listAll(): List<T>
}
