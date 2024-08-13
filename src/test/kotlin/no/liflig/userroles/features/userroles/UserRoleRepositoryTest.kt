package no.liflig.userroles.features.userroles

import no.liflig.documentstore.dao.CrudDaoJdbi
import no.liflig.userroles.common.serialization.userRolesSerializationAdapter
import no.liflig.userroles.features.userroles.domain.Role
import no.liflig.userroles.features.userroles.domain.UserRole
import no.liflig.userroles.features.userroles.persistence.UserRoleRepository
import no.liflig.userroles.features.userroles.persistence.UserRoleSearchDao
import no.liflig.userroles.testutils.createJdbiForTests
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UserRoleRepositoryTest {

  val jdbi = createJdbiForTests()

  val userRoleRepository =
      UserRoleRepository(
          crudDao =
              CrudDaoJdbi(
                  jdbi = jdbi,
                  serializationAdapter = userRolesSerializationAdapter,
                  sqlTableName = "userroles",
              ),
          searchDao =
              UserRoleSearchDao(
                  jdbi = jdbi,
                  sqlTableName = "userroles",
              ),
      )

  @BeforeEach
  fun clear() {
    userRoleRepository.listAll().forEach { userRoleRepository.delete(it.item.id, it.version) }
  }

  @Test
  fun `Role creation with valid role name succeeds`() {
    val userId = "testUser"

    val userRole =
        UserRole(
            userId = userId,
            roles =
                listOf(
                    Role(
                        roleName = "admin",
                        orgId = "org123",
                    ),
                    Role(
                        orgId = "org1234",
                        roleName = "orgMember",
                        roleValue = """{"boards": [1,2,3]}""",
                    ),
                ),
        )

    userRoleRepository.create(userRole)

    val userRoleResult = userRoleRepository.getByUserId(userId)
    assertEquals(userRole, userRoleResult?.item)
  }

  @Test
  fun `Creation with duplicate userId fails - unique index works as expected`() {
    val userId = "testUser"

    val userRole =
        UserRole(
            userId = userId,
            roles =
                listOf(
                    Role(
                        roleName = "admin",
                        orgId = "org123",
                    ),
                    Role(
                        orgId = "org1234",
                        roleName = "orgMember",
                        roleValue = """{"boards": [1,2,3]}""",
                    ),
                ),
        )
    val userRole2 = UserRole(userId = userId, roles = listOf())

    userRoleRepository.create(userRole)

    val exception = assertThrows<Exception> { userRoleRepository.create(userRole2) }
    assertEquals(true, exception.message?.contains("user_role_user_id_idx"))
  }

  @Test
  fun `Deleting a user removes it from DB`() {
    val (userRole, version) =
        userRoleRepository.create(
            UserRole(
                userId = "testUser",
                roles =
                    listOf(
                        Role(
                            roleName = "admin",
                            orgId = "org123",
                        ),
                        Role(
                            orgId = "org1234",
                            roleName = "orgMember",
                            roleValue = """{"boards": [1,2,3]}""",
                        ),
                    ),
            ))

    userRoleRepository.delete(userRole.id, version)

    val getResult = userRoleRepository.get(userRole.id)
    assertNull(getResult)
  }

  @Test
  fun `ListAll returns all users`() {
    val userId = "testUser"

    for (i in 0..2) {
      UserRole(
              userId = userId + i,
              roles = listOf(),
          )
          .apply { userRoleRepository.create(this) }
    }

    val allUsers = userRoleRepository.listAll()
    assertEquals(3, allUsers.size)
  }
}
