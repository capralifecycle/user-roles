package no.liflig.userroles.common.errorhandling

import kotlinx.serialization.KSerializer
import no.liflig.http4k.setup.createJsonBodyLens
import org.http4k.lens.BiDiBodyLens

/**
 * Creates an http4k body lens to get/set a JSON body on an HTTP request or response, using the
 * given serializer (see [createJsonBodyLens]).
 *
 * If request body parsing fails, a [PublicException] is thrown with the given message, which is
 * caught by our [CustomErrorResponseRenderer] to show the error message to the user. If
 * [includeExceptionMessage] is true, we also include the parsing exception message as details on
 * the returned error (this often contains useful information for the user about what failed, so we
 * include it by default).
 */
fun <T> createJsonBodyLensWithErrorMessage(
    serializer: KSerializer<T>,
    messageOnParsingFailure: String,
    includeExceptionMessage: Boolean = true,
): BiDiBodyLens<T> {
  return createJsonBodyLens(
      serializer,
      mapDecodingException = { e ->
        PublicException(
            messageOnParsingFailure,
            ErrorType.BAD_REQUEST,
            publicDetails = if (includeExceptionMessage) e.message else null,
            cause = e,
        )
      },
  )
}
