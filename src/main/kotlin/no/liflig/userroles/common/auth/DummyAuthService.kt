package no.liflig.userroles.common.auth

import arrow.core.Either
import arrow.core.right
import mu.KLogging
import no.liflig.http4k.AuthService
import no.liflig.http4k.GetPrincipalDeviation
import no.liflig.userroles.common.config.http4k.UserPrincipal
import org.http4k.core.Request

object DummyAuthService : AuthService<UserPrincipal>, KLogging() {
  override suspend fun getPrincipal(
      request: Request
  ): Either<GetPrincipalDeviation, UserPrincipal?> {
    return UserPrincipal(
            userId = "dummy",
        )
        .right()
  }
}
