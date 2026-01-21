package no.liflig.userroles.administration.identityprovider.cognito

import java.time.Instant
import no.liflig.userroles.administration.CreateUserRequest
import no.liflig.userroles.administration.InvitationMessageType
import no.liflig.userroles.administration.UpdateUserRequest
import no.liflig.userroles.administration.UserDataWithRoles
import no.liflig.userroles.administration.UserEmail
import no.liflig.userroles.administration.UserPhoneNumber
import no.liflig.userroles.administration.UserSearchField
import no.liflig.userroles.administration.UserUpdateData
import no.liflig.userroles.administration.identityprovider.IdentityProvider
import no.liflig.userroles.administration.identityprovider.UserDataWithoutRoles
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserRequest
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserResponse
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminUpdateUserAttributesRequest
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType
import software.amazon.awssdk.services.cognitoidentityprovider.model.DeliveryMediumType
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserType

/** Implements the [IdentityProvider] interface for AWS Cognito. */
class CognitoIdentityProvider(
    private val cognitoClient: CognitoIdentityProviderClient,
    private val userPoolId: String,
) : IdentityProvider {
  override val name = "AWS Cognito"

  /**
   * See
   * [Cognito docs](https://docs.aws.amazon.com/cognito-user-identity-pools/latest/APIReference/API_AdminGetUser.html).
   */
  override fun getUser(username: String): UserDataWithoutRoles {
    val cognitoResponse =
        cognitoClient.adminGetUser { it.username(username).userPoolId(userPoolId) }

    return mapCognitoGetResponseToUser(cognitoResponse)
  }

  /**
   * See
   * [Cognito docs](https://docs.aws.amazon.com/cognito-user-identity-pools/latest/APIReference/API_ListUsers.html).
   *
   * @param limit Number of users to return. Minimum 1, max 60.
   * @param cursor Null if this is the first fetch.
   */
  override fun listUsers(
      limit: Int,
      cursor: String?,
      searchString: String?,
      searchField: UserSearchField?,
  ): IdentityProvider.UserList {
    val cognitoFilterString = buildCognitoFilterString(searchString, searchField)

    val cognitoResponse =
        cognitoClient.listUsers { request ->
          request.limit(limit).userPoolId(userPoolId)
          cursor?.let { request.paginationToken(it) }
          cognitoFilterString?.let { request.filter(it) }
        }

    return IdentityProvider.UserList(
        users = cognitoResponse.users().map(::mapCognitoUser),
        nextCursor = cognitoResponse.paginationToken(),
    )
  }

  /**
   * See
   * [Cognito docs](https://docs.aws.amazon.com/cognito-user-identity-pools/latest/APIReference/API_AdminCreateUser.html).
   */
  override fun createUser(request: CreateUserRequest): UserDataWithoutRoles {
    val cognitoResponse = cognitoClient.adminCreateUser(mapCreateUserRequest(request))

    return mapCognitoUser(cognitoResponse.user())
  }

  /**
   * See
   * [Cognito docs](https://docs.aws.amazon.com/cognito-user-identity-pools/latest/APIReference/API_AdminUpdateUserAttributes.html).
   */
  override fun updateUser(request: UpdateUserRequest) {
    cognitoClient.adminUpdateUserAttributes(mapUpdateUserRequest(request))
  }

  /**
   * See
   * [Cognito docs](https://docs.aws.amazon.com/cognito-user-identity-pools/latest/APIReference/API_AdminDeleteUser.html).
   */
  override fun deleteUser(username: String) {
    cognitoClient.adminDeleteUser { it.username(username).userPoolId(userPoolId) }
  }

  /**
   * See
   * [Cognito docs](https://docs.aws.amazon.com/cognito-user-identity-pools/latest/APIReference/API_AdminResetUserPassword.html).
   */
  override fun resetUserPassword(username: String) {
    cognitoClient.adminResetUserPassword { it.username(username).userPoolId(userPoolId) }
  }

  private fun mapCognitoUser(cognitoUser: UserType): UserDataWithoutRoles =
      mapCognitoFieldsToUser(
          username = cognitoUser.username(),
          cognitoAttributes = cognitoUser.attributes(),
          userStatus = cognitoUser.userStatusAsString(),
          enabled = cognitoUser.enabled(),
          createdAt = cognitoUser.userCreateDate(),
      )

  private fun mapCognitoGetResponseToUser(
      cognitoGetResponse: AdminGetUserResponse,
  ): UserDataWithoutRoles =
      mapCognitoFieldsToUser(
          username = cognitoGetResponse.username(),
          cognitoAttributes = cognitoGetResponse.userAttributes(),
          userStatus = cognitoGetResponse.userStatusAsString(),
          enabled = cognitoGetResponse.enabled(),
          createdAt = cognitoGetResponse.userCreateDate(),
      )

  /** Returns a user with empty roles (see [IdentityProvider] docstring for why). */
  private fun mapCognitoFieldsToUser(
      username: String,
      cognitoAttributes: List<AttributeType>,
      userStatus: String,
      enabled: Boolean,
      createdAt: Instant,
  ): UserDataWithoutRoles {
    var userId: String? = null

    var email: String? = null
    /** If we don't receive an `email_verified` attribute, assume it's verified. */
    var emailVerified = true

    var phoneNumber: String? = null
    /** If we don't receive a `phone_number_verified` attribute, assume it's verified. */
    var phoneNumberVerified = true

    /** Use [LinkedHashMap] in order to maintain the order of attributes from Cognito. */
    val attributes = LinkedHashMap<String, String>()

    cognitoAttributes.forEach { attribute ->
      val key = attribute.name()
      val value = attribute.value()
      /** Extract certain standard attributes, put rest in [UserDataWithRoles.attributes] map. */
      when (key) {
        CognitoAttribute.SUB.attributeName -> userId = value
        CognitoAttribute.EMAIL.attributeName -> email = value
        CognitoAttribute.EMAIL_VERIFIED.attributeName -> emailVerified = value == "true"
        CognitoAttribute.PHONE_NUMBER.attributeName -> phoneNumber = value
        CognitoAttribute.PHONE_NUMBER_VERIFIED.attributeName ->
            phoneNumberVerified = value == "true"
        else -> {
          attributes[key.removePrefix(COGNITO_CUSTOM_ATTRIBUTE_PREFIX)] = value
        }
      }
    }

    if (userId == null) {
      /**
       * According to Cognito, attributes should always include `sub` (user ID):
       * https://docs.aws.amazon.com/cognito/latest/developerguide/user-pool-settings-attributes.html#cognito-user-pools-standard-attributes
       */
      throw IllegalStateException(
          "Cognito user '${username}' did not have attribute 'sub' (user ID) set, which we always expect to be present"
      )
    }

    return UserDataWithoutRoles(
        username = username,
        userId = userId,
        email = email?.let { UserEmail(value = it, verified = emailVerified) },
        phoneNumber =
            phoneNumber?.let { UserPhoneNumber(value = it, verified = phoneNumberVerified) },
        userStatus = userStatus,
        enabled = enabled,
        createdAt = createdAt,
        attributes = attributes,
    )
  }

  private fun mapCreateUserRequest(request: CreateUserRequest): AdminCreateUserRequest {
    val user = request.user
    val cognitoRequest = AdminCreateUserRequest.builder()

    cognitoRequest.userPoolId(userPoolId)
    cognitoRequest.username(user.username)

    val attributes = ArrayList<AttributeType>()
    if (user.email != null) {
      attributes.addAll(user.email.toCognitoAttributes())
    }
    if (user.phoneNumber != null) {
      attributes.addAll(user.phoneNumber.toCognitoAttributes())
    }
    for ((name, value) in user.attributes) {
      attributes.add(createAttribute(name, value))
    }
    cognitoRequest.userAttributes(attributes)

    cognitoRequest.desiredDeliveryMediums(request.invitationMessages.map { it.toCognito() })

    return cognitoRequest.build()
  }

  private fun InvitationMessageType.toCognito(): DeliveryMediumType {
    return when (this) {
      InvitationMessageType.EMAIL -> DeliveryMediumType.EMAIL
      InvitationMessageType.SMS -> DeliveryMediumType.SMS
    }
  }

  private fun mapUpdateUserRequest(request: UpdateUserRequest): AdminUpdateUserAttributesRequest {
    val user = request.user
    val cognitoRequest = AdminUpdateUserAttributesRequest.builder()

    cognitoRequest.userPoolId(userPoolId)
    cognitoRequest.username(user.username)

    val attributes = ArrayList<AttributeType>()
    if (user.email != null && user.email.value.isNotEmpty()) {
      attributes.addAll(user.email.toCognitoAttributes())
    } else {
      /**
       * In order to remove an attribute from Cognito, we must submit the attribute with a blank
       * value. If [UserUpdateData.email] is set to `null`, we do this to make sure any old email is
       * not remaining.
       */
      attributes.add(createAttribute(CognitoAttribute.EMAIL, value = ""))
      attributes.add(createAttribute(CognitoAttribute.EMAIL_VERIFIED, value = ""))
    }

    if (user.phoneNumber != null && user.phoneNumber.value.isNotEmpty()) {
      attributes.addAll(user.phoneNumber.toCognitoAttributes())
    } else {
      /**
       * In order to remove an attribute from Cognito, we must submit the attribute with a blank
       * value. If [UserUpdateData.phoneNumber] is set to `null`, we do this to make sure any old
       * phone number is not remaining.
       */
      attributes.add(createAttribute(CognitoAttribute.PHONE_NUMBER, value = ""))
      attributes.add(createAttribute(CognitoAttribute.PHONE_NUMBER_VERIFIED, value = ""))
    }

    for ((name, value) in user.attributes) {
      attributes.add(createAttribute(name, value))
    }

    cognitoRequest.userAttributes(attributes)

    return cognitoRequest.build()
  }

  /**
   * Builds a filter string for the Cognito ListUsers API, with the given search string and search
   * field.
   *
   * See
   * [Cognito docs](https://docs.aws.amazon.com/cognito-user-identity-pools/latest/APIReference/API_ListUsers.html#API_ListUsers_RequestParameters).
   *
   * Note that Cognito only supports "starts with" search, so the search string will only match
   * prefixes of the search field.
   */
  private fun buildCognitoFilterString(
      searchString: String?,
      searchField: UserSearchField?,
  ): String? {
    if (searchString.isNullOrBlank() || searchField == null) {
      return null
    }

    /**
     * ^= is a "starts with" filter (the only other available filter type is =, which is less useful
     * for search).
     */
    return "${searchField.toCognitoAttribute()} ^= \"${searchString}\""
  }
}
