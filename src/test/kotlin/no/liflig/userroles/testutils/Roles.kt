package no.liflig.userroles.testutils

import kotlinx.serialization.json.JsonElement
import no.liflig.userroles.roles.Role
import no.liflig.userroles.roles.UserRole

const val DEFAULT_TEST_USERNAME = "test.testesen"

fun createUserRole(
    username: String = DEFAULT_TEST_USERNAME,
    vararg roles: Role,
) =
    UserRole(
        username = username,
        roles = roles.asList(),
    )

fun createRole(
    applicationName: String? = "test-application",
    orgId: String? = "liflig",
    roleName: String = "admin",
    roleValue: JsonElement? = null,
) =
    Role(
        applicationName = applicationName,
        orgId = orgId,
        roleName = roleName,
        roleValue = roleValue,
    )
