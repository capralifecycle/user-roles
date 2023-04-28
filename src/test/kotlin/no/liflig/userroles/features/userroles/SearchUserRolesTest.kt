package no.liflig.userroles.features.userroles

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.liflig.snapshot.verifyJsonSnapshot
import no.liflig.userroles.features.userroles.app.ListUserRoleDto
import no.liflig.userroles.features.userroles.app.toDomain
import no.liflig.userroles.features.userroles.domain.UserRole
import no.liflig.userroles.features.userroles.domain.UserRoleRepository
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
import org.http4k.filter.ClientFilters.CustomBasicAuth.withBasicAuth
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(FlowTestExtension::class)
class SearchUserRolesTest {

  val client: HttpHandler = JavaHttpClient()

  @BeforeEach
  fun clear(services: TestServices) {
    services.clear()
    initialiseRepository(services.serviceRegistry.userRolesRepository)
  }

  @Test
  fun `Test 1 - search for admins using roleName`(services: TestServices) {
    client(
            Request(Method.GET, "http://localhost:${services.serverPort}/api/userroles")
                .query("roleName", "admin")
                .withBasicAuth(Credentials("testbruker", "testpassord")),
        )
        .apply { Assertions.assertEquals(Status.OK, this.status) }
        .useSerializer(ListUserRoleDto.serializer())
        .apply {
          verifyJsonSnapshot(
              "searchuserrolestest/responses/test-1.json",
              Json.encodeToString(this),
          )
        }
  }

  @Test
  fun `Test 2 - search for admins in specific organization`(services: TestServices) {
    client(
            Request(Method.GET, "http://localhost:${services.serverPort}/api/userroles")
                .query("roleName", "orgAdmin")
                .query("orgId", "orgId1")
                .withBasicAuth(Credentials("testbruker", "testpassord")),
        )
        .apply { Assertions.assertEquals(Status.OK, this.status) }
        .useSerializer(ListUserRoleDto.serializer())
        .apply {
          verifyJsonSnapshot(
              "searchuserrolestest/responses/test-2.json",
              Json.encodeToString(this),
          )
        }
  }

  @Test
  fun `Test 3 - search for specific organization`(services: TestServices) {
    client(
            Request(Method.GET, "http://localhost:${services.serverPort}/api/userroles")
                .query("orgId", "orgId1")
                .withBasicAuth(Credentials("testbruker", "testpassord")),
        )
        .apply { Assertions.assertEquals(Status.OK, this.status) }
        .useSerializer(ListUserRoleDto.serializer())
        .apply {
          verifyJsonSnapshot(
              "searchuserrolestest/responses/test-3.json",
              Json.encodeToString(this),
          )
        }
  }

  @Test
  fun `Test 4 - open search (no queries)`(services: TestServices) {
    client(
            Request(Method.GET, "http://localhost:${services.serverPort}/api/userroles")
                .withBasicAuth(Credentials("testbruker", "testpassord")),
        )
        .apply { Assertions.assertEquals(Status.OK, this.status) }
        .useSerializer(ListUserRoleDto.serializer())
        .apply {
          verifyJsonSnapshot(
              "searchuserrolestest/InitialUserRoles.json",
              Json.encodeToString(this),
          )
        }
  }

  @Test
  fun `Test 5 - non-existing organization returns empty`(services: TestServices) {
    client(
            Request(Method.GET, "http://localhost:${services.serverPort}/api/userroles")
                .query("orgId", "orgId6")
                .withBasicAuth(Credentials("testbruker", "testpassord")),
        )
        .apply { Assertions.assertEquals(Status.OK, this.status) }
        .useSerializer(ListUserRoleDto.serializer())
        .apply {
          verifyJsonSnapshot(
              "searchuserrolestest/responses/test-5.json",
              Json.encodeToString(this),
          )
        }
  }

  private fun initialiseRepository(userRoleRepository: UserRoleRepository) {
    readResourcesFileAsText("searchuserrolestest/InitialUserRoles.json")
        .deserializeAsListUserRolesDto()
        .apply { runBlocking { this@apply.forEach { userRoleRepository.create(it) } } }
  }
}

private fun String.deserializeAsListUserRolesDto(): List<UserRole> =
    Json.decodeFromString(ListUserRoleDto.serializer(), this).userRoles.map { it.toDomain() }
