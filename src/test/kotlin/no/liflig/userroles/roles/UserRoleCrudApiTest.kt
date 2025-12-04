package no.liflig.userroles.roles

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.shouldBe
import no.liflig.snapshot.verifyJsonSnapshot
import no.liflig.userroles.common.readJsonResource
import no.liflig.userroles.roles.api.UpdateRoleRequest
import no.liflig.userroles.roles.api.UserRoleDto
import no.liflig.userroles.testutils.TestServices
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserRoleCrudApiTest {
  // Not using @RegisterExtension here, since we only want to clear in BeforeAll
  private val services = TestServices.get()

  @BeforeAll
  fun clear() {
    services.clear()
  }

  private val userId = "user123"
  private val roles =
      listOf(
          Role(
              orgId = "orgId1",
              roleName = "orgOwner",
          ),
          Role(
              orgId = "orgId2",
              roleName = "orgAdmin",
          ),
          Role(
              orgId = "orgId3",
              roleName = "orgMember",
              roleValue = """{"boards": [1,2,3]}""",
          ),
          Role(
              roleName = "admin",
          ),
      )

  @Order(1)
  @Test
  fun `create user role`() {
    val response = services.putUserRole(userId, UpdateRoleRequest(roles))
    response.status shouldBe Status.OK

    shouldNotThrowAny { UserRoleDto.bodyLens(response) }

    verifyJsonSnapshot(
        "crudtest/create-response.json",
        response.bodyString(),
        ignoredPaths = listOf("id"),
    )
  }

  @Order(2)
  @Test
  fun `update user role`() {
    val requestBody = readJsonResource<UpdateRoleRequest>("crudtest/update-user-role-request.json")

    val response = services.putUserRole(userId, requestBody)
    response.status shouldBe Status.OK

    shouldNotThrowAny { UserRoleDto.bodyLens(response) }

    verifyJsonSnapshot(
        "crudtest/update-response.json",
        response.bodyString(),
        ignoredPaths = listOf("id"),
    )
  }

  @Order(3)
  @Test
  fun `get user role`() {
    val response = services.getUserRole(userId)
    response.status shouldBe Status.OK

    shouldNotThrowAny { UserRoleDto.bodyLens(response) }

    verifyJsonSnapshot(
        "crudtest/get-response.json",
        response.bodyString(),
        ignoredPaths = listOf("id"),
    )
  }

  @Order(4)
  @Test
  fun `delete user role`() {
    val deleteResponse = services.deleteUserRole(userId)
    deleteResponse.status shouldBe Status.OK

    val getResponse = services.getUserRole(userId)
    getResponse.status shouldBe Status.NOT_FOUND
  }
}

fun TestServices.putUserRole(userId: String, requestBody: UpdateRoleRequest): Response {
  return apiClient(
      Request(Method.PUT, "${baseUrl}/api/userroles/${userId}")
          .with(UpdateRoleRequest.bodyLens.of(requestBody))
          .withApiCredentials(),
  )
}

fun TestServices.getUserRole(userId: String): Response {
  return apiClient(
      Request(Method.GET, "${baseUrl}/api/userroles/${userId}").withApiCredentials(),
  )
}

fun TestServices.deleteUserRole(userId: String): Response {
  return apiClient(
      Request(Method.DELETE, "${baseUrl}/api/userroles/${userId}").withApiCredentials(),
  )
}
