package no.liflig.userroles.administration

import no.liflig.userroles.roles.SUPER_ADMIN_ROLE_NAME
import no.liflig.userroles.roles.UserRole

data class UserFilter(
    val searchString: String?,
    val searchField: UserSearchField?,
    val orgId: String?,
    val applicationName: String?,
    val roleName: String?,
) {
  /**
   * Returns true if the given user role has a role that matches [orgId], [applicationName] and
   * [roleName].
   */
  fun matches(userRole: UserRole): Boolean {
    /** Special case for no filter, to handle users with no roles. */
    if (orgId == null && applicationName == null && roleName == null) {
      return true
    }

    /**
     * Super-admins match all orgId / applicationName filters, since they are implicitly part of all
     * orgs and applications. We only do this if no filter is provided for `roleName`.
     */
    val matchAllOrgsAndApps =
        userRole.isSuperAdmin() && (this.roleName == null || this.roleName == SUPER_ADMIN_ROLE_NAME)

    return userRole.roles.any { role ->
      val orgIdMatches = (orgId == null || role.orgId == orgId || matchAllOrgsAndApps)

      val applicationMatches =
          (applicationName == null ||
              role.applicationName == applicationName ||
              matchAllOrgsAndApps)

      val roleNameMatches = (roleName == null || role.roleName == roleName)

      orgIdMatches && applicationMatches && roleNameMatches
    }
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

/**
 * User search fields allowed by Cognito. See
 * [Cognito docs](https://docs.aws.amazon.com/cognito-user-identity-pools/latest/APIReference/API_ListUsers.html#API_ListUsers_RequestParameters).
 *
 * Some of these fields may not be relevant for you, e.g. if they're not configured on your user
 * pool. So you probably want to expose only a subset of these search fields in the API of your
 * service that calls User Roles.
 *
 * We purposefully omit the `status` search field here, because it's tricky to handle:
 * - `status` is the `enabled` boolean on users in Cognito (see [UserDataWithRoles.enabled]), not
 *   the "user status" (CONFIRMED/UNCONFIRMED etc.).
 * - But when searching, you don't search for `enabled = true/false`, you search for `status =
 *   "Enabled"/"Disabled"`.
 * - If we wanted to allow searching on an `enabled` boolean, we would need some special handling
 *   here to map appropriately. We don't really see a use case at the moment for searching on this
 *   field, so we just drop it here.
 */
@Suppress("unused")
enum class UserSearchField(val cognitoAttribute: String) {
  USERNAME("username"),
  /** `sub` is the Open-ID Connect (OIDC) attribute for user ID. */
  USER_ID("sub"),
  EMAIL("email"),
  PHONE_NUMBER("phone_number"),
  NAME("name"),
  GIVEN_NAME("given_name"),
  FAMILY_NAME("family_name"),
  PREFERRED_USERNAME("preferred_username"),
  USER_STATUS("cognito:user_status"),
}
