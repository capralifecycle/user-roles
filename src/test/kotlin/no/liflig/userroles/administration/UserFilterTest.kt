package no.liflig.userroles.administration

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
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
            username = DEFAULT_TEST_USERNAME,
            createRole(orgId = "org2", applicationName = "app1", roleName = "role1"),
            createRole(orgId = "org1", applicationName = "app1", roleName = "role1"),
        )

    val filter = createUserFilter(orgId = "org1")
    filter.matches(userRole).shouldBeTrue()
  }

  @Test
  fun `user with SUPER_ADMIN role matches all orgs and applications`() {
    val superAdmin =
        createUserRole(
            username = DEFAULT_TEST_USERNAME,
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
