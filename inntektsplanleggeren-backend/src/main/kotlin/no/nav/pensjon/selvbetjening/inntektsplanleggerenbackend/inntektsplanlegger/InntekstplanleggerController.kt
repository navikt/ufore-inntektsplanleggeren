package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.audit.Auditor
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.inntekt.ForventedeInntekter
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.inntekt.InntekterResponse
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.simulering.SimuleringResponse
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
    fun getInntektsplanleggerenInitialData(
        @RequestHeader("pid", required=false) pidFromHeader:String?,
        @CookieValue("nav-obo", required=false) navObocookie: String?
    ): ResponseEntity<InntektsplanleggerenInitialResponse> {
        val response:ResponseEntity<InntektsplanleggerenInitialResponse>
        try {
            response =  ResponseEntity(
                inntektsPlanleggerService.constructInitialInntektsplanleggerResponse(SecurityContextUtil.getPidFromContext(), LocalDate.now().year),
                HttpStatus.OK
            )
            if (tokenService.isUserLoggedInAsSaksbehandler()) {
                auditor.auditInternalUserRead(tokenService.determineLoggedInUserId(), SecurityContextUtil.getPidFromContext())
            } else if (SecurityContextUtil.isFullmakt()) {
                auditor.auditFullmaktRead(tokenService.determineLoggedInUserId(), SecurityContextUtil.getPidFromContext())
            }
            return response
        } catch (exception: Exception) {
            throw ErrorHandler.exceptionToErrorResponse(exception, SecurityContextUtil.getPidFromContext())
        }
    }

    @GetMapping("inntekter")
    fun getInntekter(
        @RequestParam("simuleringsaar", required = true) simuleringsaar: Int,
        @RequestParam("fetchForventedeInntekter", required = false) fetchForventedeInntekter: Boolean?,
        @RequestHeader("pid", required=false) pidFromHeader:String?,
        @CookieValue("nav-obo", required=false) navObocookie: String?
    ): ResponseEntity<InntekterResponse> {
        return try {
            ResponseEntity(
                inntektsPlanleggerService.constructInntekterResponse(
                    SecurityContextUtil.getPidFromContext(),
                    simuleringsaar,
                    fetchForventedeInntekter?:true
                ), HttpStatus.OK
            )
        } catch (exception: Exception) {
            throw ErrorHandler.exceptionToErrorResponse(exception, SecurityContextUtil.getPidFromContext())
        }
    }

    @PostMapping("simuler")
    fun simuler(
        @RequestParam("simuleringsaar", required = true) simuleringsaar: Int,
        @RequestHeader("pid", required=false) pidFromHeader:String?,
        @RequestBody forventedeInntekter: ForventedeInntekter,
        @CookieValue("nav-obo", required=false) navObocookie: String?
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
        @RequestHeader("pid", required=false) pidFromHeader:String?,
        @RequestBody forventedeInntekter: ForventedeInntekter,
        @CookieValue("nav-obo", required=false) navObocookie: String?
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
                auditor.auditInternalUserCreate(tokenService.determineLoggedInUserId(), SecurityContextUtil.getPidFromContext())
            } else if (SecurityContextUtil.isFullmakt()) {
                auditor.auditFullmaktCreate(tokenService.determineLoggedInUserId(), SecurityContextUtil.getPidFromContext())
            }
            return response
        } catch (exception: Exception) {
            throw ErrorHandler.exceptionToErrorResponse(exception, SecurityContextUtil.getPidFromContext())
        }
    }

    @GetMapping("status")
    fun getStatus(
        @RequestParam("valgtaar", required = true) valgtAr: Int,
        @RequestParam("innsendingstidspunkt", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") innsendingsTidspunkt: LocalDateTime,
        @RequestHeader("pid", required=false) pidFromHeader:String?,
        @CookieValue("nav-obo", required=false) navObocookie: String?
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