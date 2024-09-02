package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.configuration.CallIdUtil
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.configuration.getCallIdFromMdc
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.util.Masker
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity

open class PersonNotFoundException(
    override var system: String,
    override var service: String,
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

data class ErrorResponse(val message: String, val callId: String)

class ErrorHandler {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(ErrorHandler::class.java)
        fun exceptionToErrorResponse(exception: Throwable, pid: String): ResponseEntity<Any> {

            return when (exception) {
                is ForbiddenException -> forbidden(exception, pid)
                is ClientException -> internalServerError(exception, pid)
                else -> internalServerError(exception, pid)
            }
        }

        private fun forbidden(exception: Throwable, pid: String): ResponseEntity<Any> {
            val callId = CallIdUtil.getCallIdFromMdc()
            logger.warn("${exception.message} PID: ${Masker.maskPid(pid)} NAV-Call-ID: $callId", exception)
            return ResponseEntity.internalServerError()
                .body(ErrorResponse(exception.message ?: "Unknown error occurred", callId))
        }

        private fun internalServerError(exception: Throwable, pid: String): ResponseEntity<Any> {
            val callId = CallIdUtil.getCallIdFromMdc()
            logger.error("${exception.message} PID: ${Masker.maskPid(pid)} NAV-Call-ID: $callId", exception)
            return ResponseEntity.internalServerError()
                .body(ErrorResponse(exception.message ?: "Unknown error occurred", callId))
        }
    }

}