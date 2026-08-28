package no.nav.ufore.inntektsplanleggeren.inntektsplanlegger

import no.nav.ufore.inntektsplanleggeren.util.getCurrentCallId
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(ManglerTilgangInntektskomponentenException::class)
    fun handleManglerTilgangInntektskomponenten(ex: ManglerTilgangInntektskomponentenException): ProblemDetail {
        log.warn("Mangler tilgang til inntektskomponenten. NAV-Call-ID: ${getCurrentCallId()}", ex)
        return ProblemDetail.forStatus(HttpStatus.FORBIDDEN).apply {
            detail = ErrorKoderTilFrontend.MANGLER_TILGANG_INNTEKTSKOMPONENTEN.name
        }
    }

    @ExceptionHandler(ForbiddenException::class)
    fun handleForbidden(ex: ForbiddenException): ProblemDetail {
        log.warn("Forbidden fra ${ex.system}/${ex.service}: ${ex.message}. NAV-Call-ID: ${getCurrentCallId()}", ex)
        return ProblemDetail.forStatus(HttpStatus.FORBIDDEN).apply {
            detail = ErrorKoderTilFrontend.FORBIDDEN_ERROR.name
        }
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ProblemDetail {
        log.error("Uventet feil: ${ex.message}. NAV-Call-ID: ${getCurrentCallId()}", ex)
        return ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR).apply {
            detail = ErrorKoderTilFrontend.GENERIC_ERROR.name
        }
    }

}

enum class ErrorKoderTilFrontend {
    GENERIC_ERROR,
    MANGLER_TILGANG_INNTEKTSKOMPONENTEN,
    FORBIDDEN_ERROR,
}