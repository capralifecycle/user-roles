package no.liflig.userroles.common.errorhandling

import no.liflig.http4k.setup.errorhandling.StandardErrorResponseBodyRenderer
import org.http4k.contract.ErrorResponseRenderer
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.lens.LensFailure

/**
 * Custom [ErrorResponseRenderer] that wraps the [StandardErrorResponseBodyRenderer] from
 * liflig-http4k-setup. To use it, set it as the `errorResponseBodyRenderer` in
 * [LifligBasicApiSetup][no.liflig.http4k.setup.LifligBasicApiSetup].
 *
 * Handles [LensFailure] when [PublicException] is the cause (which happens when using
 * [createJsonBodyLens][no.liflig.http4k.setup.createJsonBodyLens] with `mapDecodingException`),
 * mapping the exception to an error response (see [PublicException.toErrorResponse]).
 */
object CustomErrorResponseRenderer : ErrorResponseRenderer {
  override fun badRequest(lensFailure: LensFailure): Response {
    val publicException = lensFailure.cause as? PublicException
    val request = lensFailure.target as? Request
    if (publicException != null && request != null) {
      return publicException.toErrorResponse(request)
    }

    return StandardErrorResponseBodyRenderer.badRequest(lensFailure)
  }

  override fun notFound(): Response {
    return StandardErrorResponseBodyRenderer.notFound()
  }
}
