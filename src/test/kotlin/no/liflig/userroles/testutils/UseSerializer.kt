package no.liflig.userroles.testutils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import org.http4k.core.Response

fun <T> Response.useSerializer(serializer: KSerializer<T>): T =
  Json.decodeFromString(serializer, this.bodyString())
