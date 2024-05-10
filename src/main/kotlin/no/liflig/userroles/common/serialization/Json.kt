package no.liflig.userroles.common.serialization

import kotlinx.serialization.json.Json

val json: Json = Json {
  encodeDefaults = true
  ignoreUnknownKeys = true
}
