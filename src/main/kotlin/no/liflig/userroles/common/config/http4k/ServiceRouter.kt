package no.liflig.userroles.common.config.http4k

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.right
import java.util.*
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.serialization.Serializable
import no.liflig.logging.ErrorLog
import no.liflig.logging.NormalizedStatus
import no.liflig.logging.PrincipalLog
import no.liflig.logging.RequestResponseLog
import no.liflig.logging.http4k.CatchAllExceptionFilter
import no.liflig.logging.http4k.ErrorHandlerFilter
import no.liflig.logging.http4k.ErrorResponseRendererWithLogging
import no.liflig.logging.http4k.LoggingFilter
import no.liflig.logging.http4k.RequestIdMdcFilter
import no.liflig.logging.http4k.RequestLensFailureFilter
import no.liflig.userroles.features.health.HealthService
import no.liflig.userroles.features.health.healthEndpoint
import org.http4k.contract.JsonErrorResponseRenderer
import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.RequestContexts
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.core.with
import org.http4k.filter.CorsPolicy
import org.http4k.filter.ServerFilters
import org.http4k.format.Jackson
import org.http4k.lens.BiDiLens
import org.http4k.lens.RequestContextKey
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.routes

/**
 * This encapsulates the basic router setup for all of our services so that they handle requests
 * similarly.
 *
 * Ported from the liflig-http4k-utils library, which was archived at
 * https://github.com/capralifecycle/fnf-http4k-utils. To avoid depending on an archived library,
 * everything used by user-roles is ported to this file. This may be replaced in the future by a new
 * Liflig http4k library, in which case this should be replaced.
 */
class ServiceRouter<P, PL : PrincipalLog>(
    logHandler: (RequestResponseLog<PL>) -> Unit,
    principalToLog: (P) -> PL,
    corsPolicy: CorsPolicy?,
    authService: AuthService<P>,
    private val healthService: HealthService? = null,
    principalDeviationToResponse: (GetPrincipalDeviation) -> Response,
) {
  val contexts = RequestContexts()
  val requestIdChainLens = RequestContextKey.required<List<UUID>>(contexts)
  val errorLogLens = RequestContextKey.optional<ErrorLog>(contexts)
  val normalizedStatusLens = RequestContextKey.optional<NormalizedStatus>(contexts)
  val principalLens = RequestContextKey.optional<P>(contexts)

  val errorResponseRenderer =
      ErrorResponseRendererWithLogging(
          errorLogLens,
          normalizedStatusLens,
          JsonErrorResponseRenderer(Jackson),
      )

  val errorToContext: (Request, Throwable) -> Unit = { request, throwable ->
    request.with(errorLogLens of ErrorLog(throwable))
  }

  val handler = ApiHandler(principalLens, errorToContext)

  private val principalLog = { request: Request -> principalLens(request)?.let(principalToLog) }

  val coreFilters =
      ServerFilters.InitialiseRequestContext(contexts)
          .then(RequestIdMdcFilter(requestIdChainLens))
          .then(CatchAllExceptionFilter())
          .then(
              LoggingFilter(
                  principalLog,
                  errorLogLens,
                  normalizedStatusLens,
                  requestIdChainLens,
                  logHandler,
              ),
          )
          .let { if (corsPolicy != null) it.then(ServerFilters.Cors(corsPolicy)) else it }
          .then(ErrorHandlerFilter(errorLogLens))
          .then(RequestLensFailureFilter(errorResponseRenderer))
          .then(PrincipalFilter(principalLens, authService, principalDeviationToResponse))

  class RoutingBuilder<P>(
      val apiHandler: ApiHandler<P>,
      val errorResponseRenderer: ErrorResponseRendererWithLogging,
  ) {
    val additionalFilters = org.http4k.util.Appendable<Filter>()
    val routes = org.http4k.util.Appendable<RoutingHttpHandler>()
  }

  fun routingHandler(funk: RoutingBuilder<P>.() -> Unit): RoutingHttpHandler {
    val builder = RoutingBuilder(handler, errorResponseRenderer)
    builder.funk()

    var current = coreFilters
    builder.additionalFilters.all.forEach { current = current.then(it) }

    val routes = builder.routes.all + listOfNotNull(healthService?.let { healthEndpoint(it) })

    return current.then(
        routes(*(routes).toTypedArray()),
    )
  }
}

class ApiHandler<P>(
    private val principalLens: BiDiLens<Request, P?>,
    private val errorToContext: (Request, Throwable) -> Unit,
) {
  private val Request.principal: P?
    get() = principalLens(this)

  private fun P?.orUserNotAuthenticatedResponse(): Either<ErrorResponse, P> =
      this?.right() ?: notAuthenticated().left()

  private fun Either<ErrorResponse, Response>.handleError(request: Request): Response = getOrElse {
    if (it.throwable != null) {
      errorToContext(request, it.throwable)
    }
    it.response
  }

  /** Request handler that runs the request in a coroutine. */
  private fun coroutineHandler(
      block: suspend CoroutineScope.(request: Request) -> Response,
  ): HttpHandler = { request ->
    runBlocking(CoroutineName("no/liflig/http4k") + MDCContext()) { block(request) }
  }

  /**
   * Request handler that does not require authentication but provides the principal if available.
   *
   * The calling block will be called with an suspending Either binding by running the request in a
   * coroutine so we can use suspending code.
   */
  fun authNotChecked(
      block:
          suspend Raise<ErrorResponse>.(
              request: Request,
              principal: P?,
          ) -> Response,
  ): HttpHandler = coroutineHandler { request ->
    either { block(request, request.principal) }.handleError(request)
  }

  /**
   * Request handler that requires authentication and provides the [P] object for processing.
   *
   * The calling block will be called with an suspending Either binding by running the request in a
   * coroutine so we can use suspending code.
   */
  fun authed(
      block:
          suspend Raise<ErrorResponse>.(
              request: Request,
              principal: P,
          ) -> Response,
  ): HttpHandler =
      // Auth is checked inside.
      authNotChecked { request, principal ->
        block(
            request,
            principal.orUserNotAuthenticatedResponse().bind(),
        )
      }
}

/**
 * Create a [ErrorResponse] based on a [Response] while also wrapping or creating a new exception to
 * attach a stack trace to be able to track to the code location this happened in logs.
 */
fun Response.asErrorResponse(throwable: Throwable? = null, message: String? = null): ErrorResponse =
    ErrorResponse(
        if (message != null) this.with(ErrorMessage.bodyLens of ErrorMessage(message)) else this,
        // Copy the message to help viewing logs.
        RuntimeException(
            throwable.let {
              if (it != null) {
                it.message
              } else {
                "This exception is only for providing a stacktrace. No exception was thrown"
              }
            },
            throwable,
        ),
    )

fun notAuthenticated(throwable: Throwable? = null, message: String? = null): ErrorResponse =
    Response(Status.UNAUTHORIZED).asErrorResponse(throwable, message)

data class ErrorResponse(val response: Response, val throwable: Throwable?)

@Serializable
class ErrorMessage(
    val message: String,
) {
  companion object {
    val bodyLens by lazy { createBodyLens(serializer()) }
  }
}

abstract class GetPrincipalDeviation : RuntimeException()

interface AuthService<P> {
  fun getPrincipal(request: Request): Either<GetPrincipalDeviation, P?>
}

object PrincipalFilter {
  operator fun <P> invoke(
      principalLens: BiDiLens<Request, P?>,
      authService: AuthService<P>,
      principalDeviationToResponse: (GetPrincipalDeviation) -> Response,
  ): Filter = Filter { next ->
    { req ->
      authService.getPrincipal(req).fold(principalDeviationToResponse) {
        next(req.with(principalLens of it))
      }
    }
  }
}
