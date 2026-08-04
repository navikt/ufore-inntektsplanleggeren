package no.nav.ufore.inntektsplanleggeren.inntektsplanlegger

import no.nav.ufore.inntektsplanleggeren.util.getCurrentCallId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

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


class ErrorHandler {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(ErrorHandler::class.java)

        fun exceptionToErrorResponse(e: Throwable): ResponseStatusException {
            var statusCode: HttpStatus
            if(e is ForbiddenException) {
                statusCode = HttpStatus.FORBIDDEN
                logger.warn("Request failed with status: $statusCode and message: " + e.message, e)
            }
            else {
                statusCode = HttpStatus.INTERNAL_SERVER_ERROR
                logger.error("Request failed with status: $statusCode and message: " + e.message, e)
            }

            return ResponseStatusException(statusCode,
                "Request failed with status: $statusCode ${e.message?.let { "and message: \"$it\" " } ?: ""}. NAV-Call-ID: ${getCurrentCallId()}")
        }
    }
}