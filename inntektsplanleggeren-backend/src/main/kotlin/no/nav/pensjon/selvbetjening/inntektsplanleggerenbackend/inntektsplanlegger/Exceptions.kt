package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.configuration.CallIdUtil
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.configuration.getCallIdFromMdc
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.util.Masker
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

open class InitialException(val response: InntektsplanleggerenInitialResponse, override val message: String?) :
    RuntimeException(message)

open class PersonNotFoundException(
    override val system: String,
    override val service: String,
    override val message: String?,
    override val cause: Throwable?
) : ClientException(system, service, message, cause)

open class ClientException(
    open val system: String,
    open val service: String,
    override val message: String?,
    override val cause: Throwable?
) :
    RuntimeException("Error occurred when calling service $service in $system. DetailMessage:  $message", cause)

open class ForbiddenException(
    val system: String,
    val service: String,
    override val message: String?,
    override val cause: Throwable?
) :
    RuntimeException("Access denied when calling service $service in $system. DetailMessage:  $message", cause)

open class InitiationException(val response: InntektsplanleggerenInitialResponse, override val message: String?): RuntimeException(message)
data class ErrorResponse(val message: String, val callId: String)

class ErrorHandler {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger("console")

        fun handleResponseStatusException(
            statusCode: HttpStatus,
            pid: String,
            e: Throwable? = null,
            message: String? = null,
        ): ResponseStatusException {
            val failedResponseMessage =
                "Request failed with status: $statusCode ${message?.let { "and message: \"$it\" " } ?: ""}for pid ${
                    Masker.maskPid(pid)
                }. NAV-Call-ID: ${CallIdUtil.getCallIdFromMdc()}"
            when (statusCode) {
                HttpStatus.INTERNAL_SERVER_ERROR -> logger.error(failedResponseMessage, e)
                else -> logger.warn(failedResponseMessage, e)
            }

            return ResponseStatusException(statusCode, failedResponseMessage)
        }

        fun exceptionToErrorResponse(exception: Throwable, pid: String): ResponseStatusException {

            return when (exception) {
                is ForbiddenException -> forbidden(exception, pid)
                is ClientException -> internalServerError(exception, pid)
                else -> internalServerError(exception, pid)
            }
        }

        private fun forbidden(exception: Throwable, pid: String): ResponseStatusException {
            return handleResponseStatusException(HttpStatus.FORBIDDEN, pid, exception, exception.message ?: "Access denied with unknown cause")
        }

        private fun internalServerError(exception: Throwable, pid: String): ResponseStatusException{
            return handleResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, pid, exception, exception.message ?: "Unknown error occurred")
        }
    }

}