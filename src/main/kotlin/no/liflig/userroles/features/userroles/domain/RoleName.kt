package no.liflig.userroles.features.userroles.domain

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = RoleName.Serializer::class)
enum class RoleName(val value: String) {
  ADMIN("admin"),
  ORG_OWNER("orgOwner"),
  ORG_ADMIN("orgAdmin"),
  ORG_MEMBER("orgMember"),
  UNKNOWN("unknown"),
  ;

  internal object Serializer : KSerializer<RoleName> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("RoleName", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): RoleName {
      return from(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: RoleName) {
      encoder.encodeString(value.value)
    }
  }

  companion object {
    fun from(name: String): RoleName = values().find { it.value == name } ?: UNKNOWN
  }
}
