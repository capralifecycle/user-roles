package no.liflig.userroles.testutils

import java.io.File

fun readResourcesFileAsText(path: String): String {
  return File("src/test/resources/$path").readText()
}
