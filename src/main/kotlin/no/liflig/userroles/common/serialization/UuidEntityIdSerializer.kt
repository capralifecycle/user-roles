package no.liflig.userroles.common.serialization

import java.util.UUID
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import no.liflig.documentstore.entity.UuidEntityId

abstract class UuidEntityIdSerializer<T : UuidEntityId>(
    val factory: (UUID) -> T,
) : KSerializer<T> {
  override val descriptor: SerialDescriptor =
      PrimitiveSerialDescriptor("UuidEntityId", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: T) = encoder.encodeString(value.id.toString())

  override fun deserialize(decoder: Decoder): T = factory(UUID.fromString(decoder.decodeString()))
}
