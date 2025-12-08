package no.liflig.userroles.administration.api

import java.time.Instant
import no.liflig.userroles.administration.UserAdministrationService
import no.liflig.userroles.administration.UserDataWithRoles
import no.liflig.userroles.administration.UserEmail
import no.liflig.userroles.administration.UserPhoneNumber
import no.liflig.userroles.administration.UserUpdateData
import no.liflig.userroles.common.http4k.EndpointGroup
import no.liflig.userroles.roles.api.UserRoleDto

class UserAdministrationApi(userAdministrationService: UserAdministrationService) : EndpointGroup {
  override val endpoints =
      listOf(
          ListUsersEndpoint(userAdministrationService),
          CreateUserEndpoint(userAdministrationService),
          DeleteUserEndpoint(userAdministrationService),
      )

  companion object {
    const val PATH = "/administration/users"
  }
}

val EXAMPLE_USER_DATA_WITH_ROLES =
    UserDataWithRoles(
        username = "test.testesen",
        userId = "03e2d410-6591-4409-be77-6aca0833656d",
        email = UserEmail("test@example.org", verified = true),
        phoneNumber = UserPhoneNumber("12345678", verified = true),
        userStatus = "CONFIRMED",
        enabled = true,
        createdAt = Instant.parse("2025-12-03T11:32:59Z"),
        attributes = mapOf("name" to "Test Testesen"),
        roles = UserRoleDto.example.roles,
    )

val EXAMPLE_USER_UPDATE_DATA =
    UserUpdateData(
        username = "test.testesen",
        email = UserEmail("test@example.org", verified = true),
        phoneNumber = UserPhoneNumber("12345678", verified = true),
        attributes = mapOf("name" to "Test Testesen"),
        roles = UserRoleDto.example.roles,
    )
