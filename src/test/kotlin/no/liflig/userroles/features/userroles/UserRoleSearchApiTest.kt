package no.liflig.userroles.features.userroles

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.shouldBe
import kotlinx.serialization.Serializable
import no.liflig.snapshot.verifyJsonSnapshot
import no.liflig.userroles.common.readJsonResource
import no.liflig.userroles.features.userroles.api.ListUserRoleDto
import no.liflig.userroles.features.userroles.api.ListUserRolesEndpoint
import no.liflig.userroles.testutils.TestServices
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.filter.ClientFilters.CustomBasicAuth.withBasicAuth
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SearchUserRolesTest {
  // Not using @RegisterExtension here, since we only want to clear in BeforeAll
  private val services = TestServices.get()

  @BeforeAll
  fun clear() {
    services.clear()

    val testData = readJsonResource<TestUserRoles>("searchtest/initial-user-roles.json")
    testData.userRoles.forEach { services.app.userRoleRepo.create(it) }
  }

  data class TestCase(
      val name: String,
      val roleName: String? = null,
      val orgId: String? = null,
  )

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

  @ParameterizedTest
  @MethodSource("testCases")
  fun search(test: TestCase) {
    val response = services.listUserRoles(orgId = test.orgId, roleName = test.roleName)
    response.status shouldBe Status.OK

    shouldNotThrowAny { ListUserRoleDto.bodyLens(response) }

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
