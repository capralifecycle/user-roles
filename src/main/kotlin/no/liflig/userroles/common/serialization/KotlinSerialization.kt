package no.liflig.userroles.common.serialization

import kotlinx.serialization.KSerializer
import no.liflig.documentstore.entity.Entity
import no.liflig.documentstore.repository.SerializationAdapter

class KotlinSerialization<E : Entity<*>>(private val serializer: KSerializer<E>) :
    SerializationAdapter<E> {
  override fun toJson(entity: E): String = json.encodeToString(serializer, entity)

  override fun fromJson(value: String): E = json.decodeFromString(serializer, value)
}
