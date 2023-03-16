package no.liflig.userroles.common.config.http4k

import org.http4k.lens.Path
import org.http4k.lens.string

val userIdPathLens = Path.string().of("userId")
