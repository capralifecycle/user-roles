package no.liflig.userroles.administration

import kotlinx.serialization.Serializable
import no.liflig.userroles.common.serialization.SerializableInstant
import no.liflig.userroles.roles.Role
import no.liflig.userroles.roles.UserRole
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserType

/**
 * User data from Cognito joined with the user's roles from this service.
 *
 * This is the type returned by the List Users endpoint. You can copy this class into your consumer
 * service in order to deserialize responses from User Roles.
 *
 * See https://docs.aws.amazon.com/cognito-user-identity-pools/latest/APIReference/API_UserType.html
 * for more details on the user fields provided by Cognito.
 */
@Serializable
data class UserDataWithRoles(
    /**
     * Uniquely identifies the user in the Cognito user pool. Cannot be changed after the user is
     * created.
     *
     * See [preferredUsername] if you want a changeable username.
     *
     * Min length 1, max length 128.
     */
    val username: String,
    /**
     * The user's email.
     *
     * Optional attribute in Cognito. Use [emailOrThrow] if you expect this this to be set.
     */
    val email: String?,
    /**
     * The user's phone number.
     *
     * Optional attribute in Cognito. Use [phoneNumberOrThrow] if you expect this this to be set.
     */
    val phoneNumber: String?,
    /**
     * Standard Open-ID Connect field:
     *
     * _End-User's full name in displayable form including all name parts, possibly including titles
     * and suffixes, ordered according to the End-User's locale and preferences._
     *
     * https://openid.net/specs/openid-connect-core-1_0.html#StandardClaims
     *
     * Optional attribute in Cognito. Use [nameOrThrow] if you expect this to be set.
     */
    val name: String?,
    /**
     * Standard Open-ID Connect field:
     *
     * _Given name(s) or first name(s) of the End-User. Note that in some cultures, people can have
     * multiple given names; all can be present, with the names being separated by space
     * characters._
     *
     * https://openid.net/specs/openid-connect-core-1_0.html#StandardClaims
     *
     * Optional attribute in Cognito. Use [givenNameOrThrow] if you expect this to be set.
     */
    val givenName: String?,
    /**
     * Standard Open-ID Connect field:
     *
     * _Surname(s) or last name(s) of the End-User. Note that in some cultures, people can have
     * multiple family names or no family name; all can be present, with the names being separated
     * by space characters._
     *
     * https://openid.net/specs/openid-connect-core-1_0.html#StandardClaims
     *
     * Optional attribute in Cognito. Use [familyNameOrThrow] if you expect this to be set.
     */
    val familyName: String?,
    /**
     * [username] is unchangeable in Cognito. But Cognito allows you to set a "preferred_username"
     * standard attribute, which can be changed.
     *
     * Use the `preferredUsername()` getter to get the value of this field with a fallback to
     * [username] if this is not set.
     *
     * See Cognito docs for more on "preferred_username":
     * https://docs.aws.amazon.com/cognito/latest/developerguide/user-pool-settings-attributes.html#user-pool-settings-usernames
     */
    val preferredUsername: String?,
    /**
     * For Cognito, this is one of the following:
     * - UNCONFIRMED: User has been created but not confirmed.
     * - CONFIRMED: User has been confirmed.
     * - EXTERNAL_PROVIDER: User signed in with a third-party IdP.
     * - RESET_REQUIRED: User is confirmed, but the user must request a code and reset their
     *   password before they can sign in.
     * - FORCE_CHANGE_PASSWORD: The user is confirmed and the user can sign in using a temporary
     *   password, but on first sign-in, the user must change their password to a new value before
     *   doing anything else.
     *
     * https://docs.aws.amazon.com/cognito-user-identity-pools/latest/APIReference/API_UserType.html
     */
    val userStatus: String,
    /** Whether the user's account is enabled or disabled. */
    val enabled: Boolean,
    /** The date and time the user was created. */
    val createdAt: SerializableInstant,
    val roles: List<Role>,
) {
  /** See [email]. */
  fun emailOrThrow(): String = email ?: throw missingField("email")

  /** See [phoneNumber]. */
  fun phoneNumberOrThrow(): String = phoneNumber ?: throw missingField("phone number")

  /** See [name]. */
  fun nameOrThrow(): String = name ?: throw missingField("name")

  /** See [givenName]. */
  fun givenNameOrThrow(): String = givenName ?: throw missingField("given name")

  /** See [familyName]. */
  fun familyNameOrThrow(): String = familyName ?: throw missingField("family name")

  /** Returns [preferredUsername], or [username] if it's not set. */
  fun preferredUsername(): String = preferredUsername ?: username

  private fun missingField(fieldName: String): Exception =
      IllegalStateException("User '${username}' did not have ${fieldName} set")

  companion object {
    fun fromCognitoAndUserRole(cognitoUser: UserType, userRole: UserRole): UserDataWithRoles {
      var email: String? = null
      var phoneNumber: String? = null
      var name: String? = null
      var givenName: String? = null
      var familyName: String? = null
      var preferredUsername: String? = null

      cognitoUser.attributes().forEach { attribute ->
        val value = attribute.value()
        when (attribute.name()) {
          "email" -> email = value
          "phone_number" -> phoneNumber = value
          "name" -> name = value
          "given_name" -> givenName = value
          "family_name" -> familyName = value
          "preferred_username" -> preferredUsername = value
        }
      }

      return UserDataWithRoles(
          username = cognitoUser.username(),
          email = email,
          phoneNumber = phoneNumber,
          name = name,
          givenName = givenName,
          familyName = familyName,
          preferredUsername = preferredUsername,
          userStatus = cognitoUser.userStatusAsString(),
          enabled = cognitoUser.enabled(),
          createdAt = cognitoUser.userCreateDate(),
          roles = userRole.roles,
      )
    }
  }
}
