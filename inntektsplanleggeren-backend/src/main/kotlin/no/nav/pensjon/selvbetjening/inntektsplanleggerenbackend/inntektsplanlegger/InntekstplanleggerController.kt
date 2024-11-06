package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.audit.Auditor
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.inntekt.ForventedeInntekter
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.inntekt.InntekterResponse
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.simulering.SimuleringResponse
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.PenClient
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security.SecurityContextUtil
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security.TokenService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.LocalDateTime

@RestController
@RequestMapping("api")
class InntektsplanleggerController(
    private val inntektsPlanleggerService: InntektsplanleggerService,
    private val auditor: Auditor,
    private val tokenService: TokenService
) {
    private val logger: Logger = LoggerFactory.getLogger(InntektsplanleggerController::class.java)

    @GetMapping("initiate")
    fun getInntektsplanleggerenInitialData(): ResponseEntity<InntektsplanleggerenInitialResponse> {
        val response:ResponseEntity<InntektsplanleggerenInitialResponse>
        try {
            response =  ResponseEntity(
                inntektsPlanleggerService.constructInitialInntektsplanleggerResponse(SecurityContextUtil.getPidFromContext(), LocalDate.now().year),
                HttpStatus.OK
            )
            if (tokenService.isUserLoggedInAsSaksbehandler()) {
                auditor.auditInternalUserRead(tokenService.determineLoggedInUser(), SecurityContextUtil.getPidFromContext())
            } else if (SecurityContextUtil.isFullmakt()) {
                auditor.auditFullmaktRead(tokenService.determineLoggedInUser(), SecurityContextUtil.getPidFromContext())
            }
            return response
        } catch (exception: Exception) {
            throw ErrorHandler.exceptionToErrorResponse(exception, SecurityContextUtil.getPidFromContext())
        }
    }

    @GetMapping("inntekter")
    fun getInntekter(
        @RequestParam(
            "simuleringsaar",
            required = true
        ) simuleringsaar: Int
    ): ResponseEntity<InntekterResponse> {
        return try {
            ResponseEntity(
                inntektsPlanleggerService.constructInntekterResponse(
                    SecurityContextUtil.getPidFromContext(),
                    simuleringsaar
                ), HttpStatus.OK
            )
        } catch (exception: Exception) {
            throw ErrorHandler.exceptionToErrorResponse(exception, SecurityContextUtil.getPidFromContext())
        }
    }

    @PostMapping("simuler")
    fun simuler(
        @RequestParam("simuleringsaar", required = true) simuleringsaar: Int,
        @RequestBody forventedeInntekter: ForventedeInntekter
    ): ResponseEntity<SimuleringResponse> {
        return try {
            ResponseEntity(
                inntektsPlanleggerService.simulerInntektsendring(
                    SecurityContextUtil.getPidFromContext(),
                    simuleringsaar,
                    forventedeInntekter
                ), HttpStatus.OK
            )
        } catch (exception: Exception) {
            throw ErrorHandler.exceptionToErrorResponse(exception, SecurityContextUtil.getPidFromContext())
        }
    }

    @PostMapping("send")
    fun send(
        @RequestParam("simuleringsaar", required = true) simuleringsaar: Int,
        @RequestBody forventedeInntekter: ForventedeInntekter
    ): ResponseEntity<InntektsplanleggerenSendResponse> {
        val response :ResponseEntity<InntektsplanleggerenSendResponse>
        try {
            response = ResponseEntity(
                inntektsPlanleggerService.sendInntektsendring(
                    SecurityContextUtil.getPidFromContext(),
                    simuleringsaar,
                    forventedeInntekter
                ), HttpStatus.OK
            )
            if (tokenService.isUserLoggedInAsSaksbehandler()) {
                auditor.auditInternalUserCreate(tokenService.determineLoggedInUser(), SecurityContextUtil.getPidFromContext())
            } else if (SecurityContextUtil.isFullmakt()) {
                auditor.auditFullmaktCreate(tokenService.determineLoggedInUser(), SecurityContextUtil.getPidFromContext())
            }
            return response
        } catch (exception: Exception) {
            throw ErrorHandler.exceptionToErrorResponse(exception, SecurityContextUtil.getPidFromContext())
        }
    }

    @GetMapping("status")
    fun getStatus(
        @RequestParam("valgtaar", required = true) valgtAr: Int,
        @RequestParam("innsendingstidspunkt", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") innsendingsTidspunkt: LocalDateTime
    ): ResponseEntity<InntektsplanleggerenStatusResponse> {
        return try {
            ResponseEntity(
                inntektsPlanleggerService.constructStatusResponse(
                    SecurityContextUtil.getPidFromContext(),
                    valgtAr,
                    innsendingsTidspunkt.minusSeconds(3)                  //juster tidspunkt noen sekunder tilbake så vi er sikker på å få med alt
                ), HttpStatus.OK
            )
        } catch (exception: Exception) {
            throw ErrorHandler.exceptionToErrorResponse(exception, SecurityContextUtil.getPidFromContext())
        }
    }
}