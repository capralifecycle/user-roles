package no.liflig.userroles.features.userroles

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.shouldBe
import no.liflig.snapshot.verifyJsonSnapshot
import no.liflig.userroles.common.readJsonResource
import no.liflig.userroles.features.userroles.api.UpdateRoleRequest
import no.liflig.userroles.features.userroles.api.UserRoleDto
import no.liflig.userroles.testutils.FlowTestExtension
import no.liflig.userroles.testutils.TestServices
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.filter.ClientFilters.CustomBasicAuth.withBasicAuth
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(FlowTestExtension::class)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class UserRoleCrudApiTest {
  companion object {
    @BeforeAll
    @JvmStatic
    fun clear(services: TestServices) {
      services.clear()
    }
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
  fun `create user role`(services: TestServices) {
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
  fun `update user role`(services: TestServices) {
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
  fun `get user role`(services: TestServices) {
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
  fun `delete user role`(services: TestServices) {
    val deleteResponse = services.deleteUserRole(userId)
    deleteResponse.status shouldBe Status.OK

    val getResponse = services.getUserRole(userId)
    getResponse.status shouldBe Status.NOT_FOUND
  }
}

fun TestServices.putUserRole(userId: String, requestBody: UpdateRoleRequest): Response {
  return httpClient(
      Request(Method.PUT, "${baseUrl}/api/userroles/${userId}")
          .with(UpdateRoleRequest.bodyLens.of(requestBody))
          .withBasicAuth(config.api.credentials),
  )
}

fun TestServices.getUserRole(userId: String): Response {
  return httpClient(
      Request(Method.GET, "${baseUrl}/api/userroles/${userId}")
          .withBasicAuth(config.api.credentials),
  )
}

fun TestServices.deleteUserRole(userId: String): Response {
  return httpClient(
      Request(Method.DELETE, "${baseUrl}/api/userroles/${userId}")
          .withBasicAuth(config.api.credentials),
  )
}
