package no.liflig.userroles.features.userroles

import kotlinx.serialization.Serializable
import no.liflig.snapshot.verifyJsonSnapshot
import no.liflig.userroles.common.readJsonResource
import no.liflig.userroles.features.userroles.api.ListUserRoleDto
import no.liflig.userroles.features.userroles.api.ListUserRolesEndpoint
import no.liflig.userroles.testutils.FlowTestExtension
import no.liflig.userroles.testutils.TestServices
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.filter.ClientFilters.CustomBasicAuth.withBasicAuth
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

@ExtendWith(FlowTestExtension::class)
class SearchUserRolesTest {
  companion object {
    @BeforeAll
    @JvmStatic
    fun clear(services: TestServices) {
      services.clear()

      val testData = readJsonResource<TestUserRoles>("searchtest/initial-user-roles.json")
      testData.userRoles.forEach { services.app.userRoleRepo.create(it) }
    }

    data class TestCase(
        val name: String,
        val roleName: String? = null,
        val orgId: String? = null,
    )

    @JvmStatic
    fun testCases() =
        listOf(
            TestCase(
                name = "search for admins using roleName",
                roleName = "admin",
            ),
            TestCase(
                name = "search for admins in specific organization",
                roleName = "orgAdmin",
                orgId = "orgId1",
            ),
            TestCase(
                name = "search for specific organization",
                orgId = "orgId1",
            ),
            TestCase(
                name = "search without query params",
            ),
            TestCase(
                name = "non-existing organization returns empty",
                orgId = "orgId6",
            ),
        )
  }

  @ParameterizedTest
  @MethodSource("testCases")
  fun search(test: TestCase, services: TestServices) {
    val response = services.listUserRoles(orgId = test.orgId, roleName = test.roleName)
    assertThat(response.status).isEqualTo(Status.OK)

    assertThatCode { ListUserRoleDto.bodyLens(response) }.doesNotThrowAnyException()

    verifyJsonSnapshot("searchtest/${test.name.replace(' ', '-')}.json", response.bodyString())
  }
}

private fun TestServices.listUserRoles(orgId: String? = null, roleName: String? = null): Response {
  return httpClient(
      Request(Method.GET, "${baseUrl}/api/userroles")
          .with(ListUserRolesEndpoint.orgIdQuery.of(orgId))
          .with(ListUserRolesEndpoint.roleNameQuery.of(roleName))
          .withBasicAuth(config.api.credentials),
  )
}

@Serializable
data class TestUserRoles(
    val userRoles: List<UserRole>,
)
