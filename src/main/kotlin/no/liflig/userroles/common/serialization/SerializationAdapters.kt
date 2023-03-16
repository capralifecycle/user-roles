package no.liflig.userroles.common.serialization

import no.liflig.userroles.features.userroles.domain.UserRole

val userRolesSerializationAdapter = KotlinXSerializationAdapter(UserRole.serializer())
