package no.liflig.userroles.common.serialization

import java.time.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * `Instant` that's serializable with `kotlinx.serialization`. Using a typealias lets us skip
 * writing out `@file:UseSerializers` in every file where we need to serialize Instants, as per the
 * [`kotlinx.serialization` docs](https://github.com/Kotlin/kotlinx.serialization/blob/10778f3bb86e2e42433fbc7b2bcca5d647126d1d/docs/serializers.md#specifying-a-serializer-globally-using-a-typealias).
 */
typealias SerializableInstant = @Serializable(InstantSerializer::class) Instant

object InstantSerializer : KSerializer<Instant> {
  override val descriptor: SerialDescriptor =
      PrimitiveSerialDescriptor("java.time.Instant", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: Instant) = encoder.encodeString(value.toString())

  override fun deserialize(decoder: Decoder): Instant = Instant.parse(decoder.decodeString())
}
