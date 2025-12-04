package no.liflig.userroles.administration

import no.liflig.userroles.roles.SUPER_ADMIN_ROLE_NAME
import no.liflig.userroles.roles.UserRole

data class UserFilter(
    val searchString: String?,
    val searchField: UserSearchField?,
    val organizationId: String?,
    val applicationName: String?,
    val roleName: String?,
) {
  fun matches(userRole: UserRole): Boolean {
    /**
     * Super-admins match all organizationId / applicationName filters, since they are implicitly
     * part of all orgs and applications. We only do this if no filter is provided for `roleName`.
     */
    val matchAllOrganizationsAndApplications =
        userRole.isSuperAdmin() && (this.roleName == null || this.roleName == SUPER_ADMIN_ROLE_NAME)

    if (
        organizationId != null &&
            userRole.roles.none { it.orgId == this.organizationId } &&
            !matchAllOrganizationsAndApplications
    ) {
      return false
    }

    if (
        applicationName != null &&
            userRole.roles.none { it.applicationName == this.applicationName } &&
            !matchAllOrganizationsAndApplications
    ) {
      return false
    }

    if (roleName != null && userRole.roles.none { it.roleName == this.roleName }) {
      return false
    }

    return true
  }

  fun getCognitoFilterString(): String? {
    if (searchString.isNullOrBlank() || searchField == null) {
      return null
    }

    /**
     * This is the filter syntax used by the Cognito ListUsers API:
     * https://docs.aws.amazon.com/cognito-user-identity-pools/latest/APIReference/API_ListUsers.html#API_ListUsers_RequestParameters
     *
     * ^= is a "starts with" filter (the only other available filter type is =, which is less useful
     * for search).
     */
    return "${searchField.cognitoAttribute} ^= \"${searchString}\""
  }
}

enum class UserSearchField(val cognitoAttribute: String) {
  USERNAME(CognitoUserAttributes.USERNAME),
  EMAIL(CognitoUserAttributes.EMAIL),
  PHONE_NUMBER(CognitoUserAttributes.PHONE_NUMBER),
  NAME(CognitoUserAttributes.NAME),
  GIVEN_NAME(CognitoUserAttributes.GIVEN_NAME),
  FAMILY_NAME(CognitoUserAttributes.FAMILY_NAME),
  PREFERRED_USERNAME(CognitoUserAttributes.PREFERRED_USERNAME),
}

private object CognitoUserAttributes {
  const val USERNAME = "username"
  const val EMAIL = "email"
  const val PHONE_NUMBER = "phone_number"
  const val NAME = "name"
  const val GIVEN_NAME = "given_name"
  const val FAMILY_NAME = "family_name"
  const val PREFERRED_USERNAME = "preferred_username"
}
