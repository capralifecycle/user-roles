package no.liflig.userroles.roles

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.serialization.json.JsonPrimitive
import no.liflig.userroles.testutils.createJdbiForTests
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UserRoleRepositoryTest {
  private val jdbi = createJdbiForTests()

  private val userRoleRepository = UserRoleRepository(jdbi)

  @BeforeEach
  fun clear() {
    userRoleRepository.listAll().forEach { userRoleRepository.delete(it.data.id, it.version) }
  }

  @Test
  fun `Role creation with valid role name succeeds`() {
    val username = "testUser"

    val userRole =
        UserRole(
            username = username,
            roles =
                listOf(
                    Role(
                        roleName = "admin",
                        orgId = "org123",
                    ),
                    Role(
                        orgId = "org1234",
                        roleName = "orgMember",
                        roleValue = JsonPrimitive("test"),
                    ),
                ),
        )

    userRoleRepository.create(userRole)

    val userRoleResult = userRoleRepository.getByUsername(username)
    userRole shouldBe userRoleResult?.data
  }

  @Test
  fun `Creation with duplicate username fails - unique index works as expected`() {
    val username = "testUser"

    val userRole =
        UserRole(
            username = username,
            roles =
                listOf(
                    Role(
                        roleName = "admin",
                        orgId = "org123",
                    ),
                    Role(
                        orgId = "org1234",
                        roleName = "orgMember",
                        roleValue = JsonPrimitive("test"),
                    ),
                ),
        )
    val userRole2 = UserRole(username = username, roles = listOf())

    userRoleRepository.create(userRole)

    val exception = shouldThrow<Exception> { userRoleRepository.create(userRole2) }
    exception.message shouldContain UserRoleRepository.USERNAME_UNIQUE_INDEX_NAME
  }

  @Test
  fun `Deleting a user removes it from DB`() {
    val (userRole, version) =
        userRoleRepository.create(
            UserRole(
                username = "testUser",
                roles =
                    listOf(
                        Role(
                            roleName = "admin",
                            orgId = "org123",
                        ),
                        Role(
                            orgId = "org1234",
                            roleName = "orgMember",
                            roleValue = JsonPrimitive("test"),
                        ),
                    ),
            )
        )

    userRoleRepository.delete(userRole.id, version)

    val getResult = userRoleRepository.get(userRole.id)
    getResult.shouldBeNull()
  }

  @Test
  fun `ListAll returns all users`() {
    val username = "testUser"

    for (i in 0..2) {
      UserRole(
              username = username + i,
              roles = listOf(),
          )
          .apply { userRoleRepository.create(this) }
    }

    val allUsers = userRoleRepository.listAll()
    assertEquals(3, allUsers.size)
  }
}
