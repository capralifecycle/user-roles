package no.liflig.userroles.common.repository.domain

import no.liflig.documentstore.entity.Entity
import no.liflig.documentstore.entity.EntityId

interface Repository<T : Entity<ID>, ID : EntityId> {
  fun create(item: T): T
  fun get(id: ID): T?
  fun update(item: T): T
  fun delete(item: T)
  fun listAll(): List<T>
}
