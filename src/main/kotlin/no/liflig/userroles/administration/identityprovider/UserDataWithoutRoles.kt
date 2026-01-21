package no.liflig.userroles.administration.identityprovider

import kotlinx.serialization.Serializable
import no.liflig.userroles.administration.UserDataWithRoles
import no.liflig.userroles.administration.UserEmail
import no.liflig.userroles.administration.UserPhoneNumber
import no.liflig.userroles.common.serialization.SerializableInstant
import no.liflig.userroles.roles.Role

/**
 * User data from an identity provider (e.g. AWS Cognito), before it's joined with user roles from
 * this service.
 *
 * See [UserDataWithRoles] for documentation of the fields here.
 */
@Serializable
data class UserDataWithoutRoles(
    val username: String,
    val userId: String,
    val email: UserEmail?,
    val phoneNumber: UserPhoneNumber?,
    val userStatus: String,
    val enabled: Boolean,
    val createdAt: SerializableInstant,
    val attributes: Map<String, String>,
) {
  fun withRoles(roles: List<Role>) =
      UserDataWithRoles(
          username = this.username,
          userId = this.userId,
          email = this.email,
          phoneNumber = this.phoneNumber,
          userStatus = this.userStatus,
          enabled = this.enabled,
          createdAt = this.createdAt,
          attributes = this.attributes,
          roles = roles,
      )
}
