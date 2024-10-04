package no.liflig.userroles.common.http4k

import org.http4k.contract.ContractRoute

/**
 * An API endpoint for some resource.
 *
 * You should use the `route` method to configure API schema metadata for the endpoint, and define a
 * separate `handler(Request): Response` method for the actual request handling. Cleanly separating
 * these two makes the endpoint easier to reason about. Example:
 * ```
 * class ListExampleEntities(
 *     private val exampleRepo: ExampleRepository,
 * ) : Endpoint {
 *   override fun route(): ContractRoute {
 *     val path = ExampleApi.PATH
 *     val spec =
 *         path.meta {
 *           summary = "Get all example entities"
 *           operationId = "getExampleEntities"
 *           returning(Status.OK, ExampleDto.listBodyLens to listOf(ExampleDto.example))
 *         }
 *     return spec.bindContract(Method.GET) to ::handler
 *   }
 *
 *   private fun handler(request: Request): Response {
 *     val exampleEntities = exampleRepo.listAll()
 *
 *     return Response(Status.OK)
 *         .with(ExampleDto.listBodyLens.of(exampleEntities.map { ExampleDto.from(it) }))
 *   }
 * }
 * ```
 *
 * If your endpoint takes path parameters (using http4k's `Path`), your `handler` must be function
 * that takes path params and returns a `(Request) -> Response` function. Example:
 * ```
 * class GetExampleEntity(
 *     private val exampleRepo: ExampleRepository,
 * ) : Endpoint {
 *   override fun route(): ContractRoute {
 *     val path = ExampleApi.PATH / Path.of("exampleId")
 *     val spec =
 *         path.meta {
 *           summary = "Get example entity"
 *           operationId = "getExampleEntity"
 *           returning(Status.OK, ExampleDto.bodyLens to ExampleDto.example)
 *           returning(Status.NOT_FOUND)
 *         }
 *     return spec.bindContract(Method.GET) to ::handler
 *   }
 *
 *   private fun handler(exampleId: String) =
 *       fun(_: Request): Response {
 *         val exampleEntity = exampleRepo.get(exampleId) ?: return Response(Status.NOT_FOUND)
 *
 *         return Response(Status.OK).with(ExampleDto.bodyLens.of(ExampleDto.from(exampleEntity)))
 *       }
 * }
 * ```
 */
interface Endpoint {
  fun route(): ContractRoute
}

/**
 * A grouping of endpoints for an API resource.
 *
 * Typically, endpoints in this group will share a common base path defined on the group. Example:
 * ```
 * class ExampleApi(exampleRepo: ExampleRepository) : EndpointGroup {
 *   override val endpoints =
 *       listOf(
 *           GetExampleEntity(exampleRepo),
 *           ListExampleEntities(exampleRepo),
 *       )
 *
 *   companion object {
 *     const val PATH = "/example"
 *   }
 * }
 * ```
 *
 * Endpoints then use `ExampleApi.PATH` when configuring their routes (see example in [Endpoint]).
 */
interface EndpointGroup {
  val endpoints: List<Endpoint>
}
