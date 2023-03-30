package no.liflig.userroles.features.userroles

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.liflig.snapshot.verifyJsonSnapshot
import no.liflig.userroles.features.userroles.app.RoleDto
import no.liflig.userroles.features.userroles.app.UserRoleDto
import no.liflig.userroles.features.userroles.app.routes.UpdateUserRole
import no.liflig.userroles.features.userroles.domain.RoleName
import no.liflig.userroles.testutils.FlowTestExtension
import no.liflig.userroles.testutils.TestServices
import no.liflig.userroles.testutils.readResourcesFileAsText
import no.liflig.userroles.testutils.useSerializer
import org.http4k.client.JavaHttpClient
import org.http4k.core.Credentials
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.filter.ClientFilters.CustomBasicAuth.withBasicAuth
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(FlowTestExtension::class)
class UserRoleCrudApiFlowTest {

  val client: HttpHandler = JavaHttpClient()

  @BeforeEach
  fun clear(services: TestServices) {
    services.clear()
  }

  @Test
  fun crudApiFlowTest(services: TestServices) {
    val userId = "user123"
    val roles =
        listOf(
            RoleDto(
                orgId = "orgId1",
                roleName = RoleName.ORG_OWNER,
            ),
            RoleDto(
                orgId = "orgId2",
                roleName = RoleName.ORG_ADMIN,
            ),
            RoleDto(
                orgId = "orgId3",
                roleName = RoleName.ORG_MEMBER,
                roleValue = """{"boards": [1,2,3]}""",
            ),
            RoleDto(
                roleName = RoleName.ADMIN,
            ),
        )

    client(
            Request(Method.PUT, "http://localhost:${services.serverPort}/api/userroles/$userId")
                .with(
                    UpdateUserRole.UpdateRoleRequest.bodyLens of
                        UpdateUserRole.UpdateRoleRequest(
                            roles = roles,
                        ),
                )
                .withBasicAuth(Credentials("testbruker", "testpassord")),
        )
        .apply { assertEquals(Status.OK, this.status) }
        .useSerializer(UserRoleDto.serializer())
        .apply {
          verifyJsonSnapshot(
              "userrolecrudapiflowtestfiles/CreateUserRoleResponse.json",
              Json.encodeToString(this),
              ignoredPaths = listOf("id"),
          )
        }

    readResourcesFileAsText("userrolecrudapiflowtestfiles/UpdateUserRoleRequest.json")
        .deserializeAsUpdateRoleRequest()
        .let {
          client(
              Request(Method.PUT, "http://localhost:${services.serverPort}/api/userroles/$userId")
                  .with(
                      UpdateUserRole.UpdateRoleRequest.bodyLens of it,
                  )
                  .withBasicAuth(Credentials("testbruker", "testpassord")),
          )
        }
        .apply { assertEquals(Status.OK, this.status) }
        .useSerializer(UserRoleDto.serializer())
        .apply {
          verifyJsonSnapshot(
              "userrolecrudapiflowtestfiles/CreateUserRoleResponse-1.json",
              Json.encodeToString(this),
              ignoredPaths = listOf("id"),
          )
        }

    client(
            Request(Method.GET, "http://localhost:${services.serverPort}/api/userroles/$userId")
                .withBasicAuth(Credentials("testbruker", "testpassord")),
        )
        .apply { assertEquals(Status.OK, this.status) }
        .useSerializer(UserRoleDto.serializer())
        .apply {
          verifyJsonSnapshot(
              "userrolecrudapiflowtestfiles/GetUserRoleResponse-1.json",
              Json.encodeToString(this),
              ignoredPaths = listOf("id"),
          )
        }

    client(
            Request(Method.DELETE, "http://localhost:${services.serverPort}/api/userroles/$userId")
                .withBasicAuth(Credentials("testbruker", "testpassord")),
        )
        .apply { assertEquals(Status.OK, this.status) }

    client(
            Request(Method.GET, "http://localhost:${services.serverPort}/api/userroles/$userId")
                .withBasicAuth(Credentials("testbruker", "testpassord")),
        )
        .apply { assertEquals(Status.NOT_FOUND, this.status) }
  }
}

private fun String.deserializeAsUpdateRoleRequest(): UpdateUserRole.UpdateRoleRequest =
    Json.decodeFromString(UpdateUserRole.UpdateRoleRequest.serializer(), this)
