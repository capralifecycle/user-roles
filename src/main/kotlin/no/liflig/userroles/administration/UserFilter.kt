package no.liflig.userroles.administration

import no.liflig.userroles.administration.identityprovider.cognito.toCognitoAttribute
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
}

/**
 * User search fields allowed by our List Users API. Based on the search fields allowed by AWS
 * Cognito (see [toCognitoAttribute]).
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
enum class UserSearchField {
  USERNAME,
  USER_ID,
  EMAIL,
  PHONE_NUMBER,
  NAME,
  GIVEN_NAME,
  FAMILY_NAME,
  PREFERRED_USERNAME,
  USER_STATUS,
}
