package no.liflig.userroles.common.serialization

import java.util.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object UuidSerializer : KSerializer<UUID> {
  override val descriptor: SerialDescriptor =
      PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: UUID) = encoder.encodeString(value.toString())

  override fun deserialize(decoder: Decoder): UUID = UUID.fromString(decoder.decodeString())
}
