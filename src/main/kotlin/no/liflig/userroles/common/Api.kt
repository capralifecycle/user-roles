package no.liflig.userroles.common

import org.http4k.contract.ContractRoute

interface Api {
  val basePath: String
  val endpoints: List<Endpoint>

  fun routes(): List<ContractRoute> = endpoints.map { it.route(basePath) }
}

interface Endpoint {
  fun route(basePath: String): ContractRoute
}
