package no.liflig.userroles.administration

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import no.liflig.userroles.testutils.DEFAULT_TEST_USERNAME
import no.liflig.userroles.testutils.createRole
import no.liflig.userroles.testutils.createUserRole
import org.junit.jupiter.api.Test

/**
 * We already test some parts of user filtering in [UserAdministrationServiceTest]. But we test more
 * edge cases here.
 */
class UserFilterTest {
  @Test
  fun `filter matches if any of the user's roles is a match`() {
    val userRole =
        createUserRole(
            DEFAULT_TEST_USERNAME,
            createRole(orgId = "org2", applicationName = "app1", roleName = "role1"),
            createRole(orgId = "org1", applicationName = "app1", roleName = "role1"),
        )

    val filter = createUserFilter(orgId = "org1")
    filter.matches(userRole).shouldBeTrue()
  }

  @Test
  fun `filter only matches if user has a role that fulfills all dimensions`() {
    val filter = createUserFilter(orgId = "org1", applicationName = "app1", roleName = "role1")

    val userRole =
        createUserRole(
            DEFAULT_TEST_USERNAME,
            // Roles that match the filter in all dimensions except 1
            createRole(orgId = "org1", applicationName = "app1", roleName = "role2"),
            createRole(orgId = "org1", applicationName = "app2", roleName = "role1"),
            createRole(orgId = "org2", applicationName = "app1", roleName = "role1"),
        )
    filter.matches(userRole).shouldBeFalse()

    val matchingUserRole =
        userRole.copy(
            roles =
                userRole.roles +
                    createRole(orgId = "org1", applicationName = "app1", roleName = "role1")
        )
    filter.matches(matchingUserRole).shouldBeTrue()
  }

  @Test
  fun `user with SUPER_ADMIN role matches all orgs and applications`() {
    val superAdmin =
        createUserRole(
            DEFAULT_TEST_USERNAME,
            createRole(roleName = "SUPER_ADMIN", orgId = null, applicationName = null),
        )

    val orgFilter = createUserFilter(orgId = "org1")
    orgFilter.matches(superAdmin).shouldBeTrue()

    val appFilter = createUserFilter(orgId = "app1")
    appFilter.matches(superAdmin).shouldBeTrue()

    val orgAndRoleFilter = createUserFilter(orgId = "org1", roleName = "MEMBER")
    orgAndRoleFilter.matches(superAdmin).shouldBeFalse()

    val appAndRoleFilter = createUserFilter(applicationName = "app1", roleName = "MEMBER")
    appAndRoleFilter.matches(superAdmin).shouldBeFalse()
  }

  @Test
  fun `empty filter always matches`() {
    val emptyFilter = createUserFilter()

    val userWithoutRoles = createUserRole()
    userWithoutRoles.roles.shouldBeEmpty()

    emptyFilter.matches(userWithoutRoles).shouldBeTrue()

    val userWithRole = createUserRole(DEFAULT_TEST_USERNAME, createRole())

    emptyFilter.matches(userWithRole).shouldBeTrue()
  }
}

fun createUserFilter(
    searchString: String? = null,
    searchField: UserSearchField? = null,
    orgId: String? = null,
    applicationName: String? = null,
    roleName: String? = null,
) =
    UserFilter(
        searchString = searchString,
        searchField = searchField,
        orgId = orgId,
        applicationName = applicationName,
        roleName = roleName,
    )
