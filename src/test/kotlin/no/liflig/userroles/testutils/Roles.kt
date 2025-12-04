package no.liflig.userroles.testutils

import no.liflig.userroles.roles.Role
import no.liflig.userroles.roles.UserRole

const val DEFAULT_TEST_USERNAME = "test.testesen"

fun createUserRole(
    userId: String = DEFAULT_TEST_USERNAME,
    vararg roles: Role,
) =
    UserRole(
        userId = userId,
        roles = roles.asList(),
    )

fun createRole(
    applicationName: String? = "test-application",
    orgId: String? = "liflig",
    roleName: String = "admin",
    roleValue: String? = null,
) =
    Role(
        applicationName = applicationName,
        orgId = orgId,
        roleName = roleName,
        roleValue = roleValue,
    )
