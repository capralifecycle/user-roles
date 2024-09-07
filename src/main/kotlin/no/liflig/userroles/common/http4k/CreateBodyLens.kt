package no.liflig.userroles.common.http4k

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import org.http4k.core.Body
import org.http4k.core.ContentType
import org.http4k.lens.BiDiBodyLens
import org.http4k.lens.string

private val json = Json {
  encodeDefaults = true
  ignoreUnknownKeys = true
}

fun <T> createBodyLens(serializer: KSerializer<T>): BiDiBodyLens<T> {
  return Body.string(ContentType.APPLICATION_JSON)
      .map(
          { json.decodeFromString(serializer, it) },
          { json.encodeToString(serializer, it) },
      )
      .toLens()
}
