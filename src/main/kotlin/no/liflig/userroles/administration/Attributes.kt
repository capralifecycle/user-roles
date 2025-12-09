package no.liflig.userroles.administration

import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType

/**
 * [Standard attributes in
 * Cognito](https://docs.aws.amazon.com/cognito/latest/developerguide/user-pool-settings-attributes.html#cognito-user-pools-standard-attributes),
 * based on the OpenID Connect (OIDC) specification.
 *
 * See [Open-ID Connect](https://openid.net/specs/openid-connect-core-1_0.html#StandardClaims) for
 * more.
 *
 * All custom (non-standard) attributes must be prefixed by `custom:` in Cognito. We enforce this in
 * [createAttribute].
 */
enum class StandardAttribute(val attributeName: String) {
  /**
   * From OIDC: "End-User's full name in displayable form including all name parts, possibly
   * including titles and suffixes, ordered according to the End-User's locale and preferences."
   */
  NAME("name"),
  /**
   * From OIDC: "Surname(s) or last name(s) of the End-User. Note that in some cultures, people can
   * have multiple family names or no family name; all can be present, with the names being
   * separated by space characters."
   */
  FAMILY_NAME("family_name"),
  /**
   * From OIDC: "Given name(s) or first name(s) of the End-User. Note that in some cultures, people
   * can have multiple given names; all can be present, with the names being separated by space
   * characters."
   */
  GIVEN_NAME("given_name"),
  /**
   * From OIDC: "Casual name of the End-User that may or may not be the same as the given_name. For
   * instance, a nickname value of Mike might be returned alongside a given_name value of Michael."
   */
  NICKNAME("nickname"),
  /**
   * From OIDC: "Shorthand name by which the End-User wishes to be referred to at the RP, such as
   * janedoe or j.doe. This value MAY be any valid JSON string including special characters such
   * as @, /, or whitespace. The RP MUST NOT rely upon this value being unique, as discussed in
   * Section 5.7."
   */
  PREFERRED_USERNAME("preferred_username"),
  /**
   * From OIDC: "URL of the End-User's profile page. The contents of this Web page SHOULD be about
   * the End-User."
   */
  PROFILE("profile"),
  /**
   * From OIDC: "URL of the End-User's profile picture. This URL MUST refer to an image file (for
   * example, a PNG, JPEG, or GIF image file), rather than to a Web page containing an image. Note
   * that this URL SHOULD specifically reference a profile photo of the End-User suitable for
   * displaying when describing the End-User, rather than an arbitrary photo taken by the End-User."
   */
  PICTURE("picture"),
  /**
   * From OIDC: "URL of the End-User's Web page or blog. This Web page SHOULD contain information
   * published by the End-User or an organization that the End-User is affiliated with."
   */
  WEBSITE("website"),
  /**
   * From OIDC: "End-User's gender. Values defined by this specification are female and male. Other
   * values MAY be used when neither of the defined values are applicable."
   */
  GENDER("gender"),
  /**
   * From Cognito: "Value must be a valid 10 character date in the format YYYY-MM-DD."
   *
   * From OIDC: "End-User's birthday, represented as an ISO 8601-1 [ISO8601‑1] YYYY-MM-DD format.
   * The year MAY be 0000, indicating that it is omitted. To represent only the year, YYYY format is
   * allowed. Note that depending on the underlying platform's date related function, providing just
   * year can result in varying month and day, so the implementers need to take this factor into
   * account to correctly process the dates."
   */
  BIRTHDATE("birthdate"),
  /**
   * From OIDC: "String from IANA Time Zone Database representing the End-User's time zone. For
   * example, Europe/Paris or America/Los_Angeles."
   */
  ZONEINFO("zoneinfo"),
  /**
   * From OIDC: "End-User's locale, represented as a BCP47 (RFC 5646) language tag. This is
   * typically an ISO 639 Alpha-2 (ISO 639) language code in lowercase and an ISO 3166-1 Alpha-2
   * [ISO3166‑1] country code in uppercase, separated by a dash. For example, en-US or fr-CA. As a
   * compatibility note, some implementations have used an underscore as the separator rather than a
   * dash, for example, en_US; Relying Parties MAY choose to accept this locale syntax as well."
   */
  LOCALE("locale"),
  /**
   * From OIDC: "Time the End-User's information was last updated. Its value is a JSON number
   * representing the number of seconds from 1970-01-01T00:00:00Z as measured in UTC until the
   * date/time."
   */
  UPDATED_AT("updated_at"),
  /**
   * From OIDC: "End-User's preferred postal address. The value of the address member is a JSON
   * structure containing some or all of the members defined in Section 5.1.1."
   */
  ADDRESS("address"),
  /**
   * From Cognito: "Value must be a valid email address string following the standard email format
   * with @ symbol and domain, up to 2048 characters in length."
   *
   * From OIDC: "End-User's preferred e-mail address. Its value MUST conform to the RFC 5322
   * addr-spec syntax. The RP MUST NOT rely upon this value being unique, as discussed in Section
   * 5.7."
   */
  EMAIL("email"),
  /**
   * From OIDC: "True if the End-User's e-mail address has been verified; otherwise false. When this
   * Claim Value is true, this means that the OP took affirmative steps to ensure that this e-mail
   * address was controlled by the End-User at the time the verification was performed. The means by
   * which an e-mail address is verified is context specific, and dependent upon the trust framework
   * or contractual agreements within which the parties are operating."
   */
  EMAIL_VERIFIED("email_verified"),
  /**
   * From OIDC: "End-User's preferred telephone number. E.164 is RECOMMENDED as the format of this
   * Claim, for example, +1 (425) 555-1212 or +56 (2) 687 2400. If the phone number contains an
   * extension, it is RECOMMENDED that the extension be represented using the RFC 3966 extension
   * syntax, for example, +1 (604) 555-1234;ext=5678."
   */
  PHONE_NUMBER("phone_number"),
  /**
   * From OIDC: "True if the End-User's phone number has been verified; otherwise false. When this
   * Claim Value is true, this means that the OP took affirmative steps to ensure that this phone
   * number was controlled by the End-User at the time the verification was performed. The means by
   * which a phone number is verified is context specific, and dependent upon the trust framework or
   * contractual agreements within which the parties are operating. When true, the phone_number
   * Claim MUST be in E.164 format and any extensions MUST be represented in RFC 3966 format."
   */
  PHONE_NUMBER_VERIFIED("phone_number_verified"),
  /**
   * See [UserDataWithRoles.userId].
   *
   * From Cognito: "Index and search your users based on the sub attribute. The sub attribute is a
   * unique user identifier within each user pool. Users can change attributes like phone_number and
   * email."
   *
   * From OIDC: "Subject - Identifier for the End-User at the Issuer."
   */
  SUB("sub"),
}

fun createAttribute(attribute: StandardAttribute, value: String): AttributeType {
  return AttributeType.builder().name(attribute.attributeName).value(value).build()
}

/**
 * Creates a Cognito attribute with the given name and value.
 *
 * If the name is a [StandardAttribute], then it uses the name as-is. Otherwise, it adds the
 * [COGNITO_CUSTOM_ATTRIBUTE_PREFIX].
 */
fun createAttribute(name: String, value: String): AttributeType {
  val standardAttribute = StandardAttribute.entries.find { it.attributeName == name }
  if (standardAttribute != null) {
    return createAttribute(attribute = standardAttribute, value)
  }

  val nameWithPrefix =
      if (name.startsWith(COGNITO_CUSTOM_ATTRIBUTE_PREFIX)) {
        name
      } else {
        "${COGNITO_CUSTOM_ATTRIBUTE_PREFIX}${name}"
      }

  return AttributeType.builder().name(nameWithPrefix).value(value).build()
}

/**
 * Custom attribute names must be prefixed by "custom:".
 *
 * See
 * [Cognito docs](https://docs.aws.amazon.com/cognito-user-identity-pools/latest/APIReference/API_AdminCreateUser.html).
 */
const val COGNITO_CUSTOM_ATTRIBUTE_PREFIX = "custom:"
