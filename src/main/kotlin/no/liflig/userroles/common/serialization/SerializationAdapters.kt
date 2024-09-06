package no.liflig.userroles.common.serialization

import no.liflig.userroles.features.userroles.UserRole

val userRolesSerializationAdapter = KotlinSerialization(UserRole.serializer())
