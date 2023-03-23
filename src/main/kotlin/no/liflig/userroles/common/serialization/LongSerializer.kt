package no.liflig.userroles.common.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import no.liflig.documentstore.entity.Version

abstract class LongSerializer(
    val factory: (Long) -> Version,
) : KSerializer<Version> {
  override val descriptor: SerialDescriptor =
      PrimitiveSerialDescriptor("CustomLongSerializer", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: Version) =
      encoder.encodeString(value.value.toString())

  override fun deserialize(decoder: Decoder): Version = factory(decoder.decodeString().toLong())
}
