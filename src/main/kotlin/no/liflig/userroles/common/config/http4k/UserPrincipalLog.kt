package no.liflig.userroles.common.config.http4k

import kotlinx.serialization.Serializable
import no.liflig.logging.PrincipalLog

@Serializable
data class UserPrincipalLog(
    val userId: String,
) : PrincipalLog

data class UserPrincipal(
    val userId: String,
)

fun UserPrincipal.toLog(): UserPrincipalLog =
    UserPrincipalLog(
        userId = userId,
    )
