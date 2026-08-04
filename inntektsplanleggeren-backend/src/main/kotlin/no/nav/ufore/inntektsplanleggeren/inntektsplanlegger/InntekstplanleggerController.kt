package no.nav.ufore.inntektsplanleggeren.inntektsplanlegger

import no.nav.ufore.inntektsplanleggeren.audit.Auditor
import no.nav.ufore.inntektsplanleggeren.inntektsplanlegger.inntekt.ForventedeInntekter
import no.nav.ufore.inntektsplanleggeren.inntektsplanlegger.inntekt.InntekterResponse
import no.nav.ufore.inntektsplanleggeren.inntektsplanlegger.simulering.SimuleringResponse
import no.nav.ufore.inntektsplanleggeren.inntektsplanlegger.startside.StartsideData
import no.nav.ufore.inntektsplanleggeren.inntektsplanlegger.startside.StartsideService
import no.nav.ufore.inntektsplanleggeren.person.PersonService
import no.nav.ufore.inntektsplanleggeren.security.SecurityContextUtil
import no.nav.ufore.inntektsplanleggeren.security.TokenService
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
    private val tokenService: TokenService,
    private val personService: PersonService,
    private val startsideService: StartsideService
) {

    @GetMapping("veilederbanner")
    fun hentVeilederBannerInfo(): ResponseEntity<VeilederBannerInfo>{
        try {
            val pid = SecurityContextUtil.getPidFromContext()
            val borgerNavn = personService.getNavn(pid)
            val veilederNavn = tokenService.determineLoggedInUser()

            return ResponseEntity.status(HttpStatus.OK).body(VeilederBannerInfo(pid, borgerNavn ?: "", veilederNavn))
                .also {
                    if (tokenService.isUserLoggedInAsSaksbehandler()) {
                        auditor.auditInternalUserRead(
                            tokenService.determineLoggedInUserId(),
                            SecurityContextUtil.getPidFromContext()
                        )
                    } else if (SecurityContextUtil.isFullmakt()) {
                        auditor.auditFullmaktRead(
                            tokenService.determineLoggedInUserId(),
                            SecurityContextUtil.getPidFromContext()
                        )
                    }
                }
        } catch (exception: Exception) {
            throw ErrorHandler.exceptionToErrorResponse(exception)
        }
    }

    @GetMapping("startside")
    fun getInntektsplanleggerenInitialData(): ResponseEntity<StartsideData> {
        try {
            return ResponseEntity(
                startsideService.hentStartsideData(
                    SecurityContextUtil.getPidFromContext(),
                    LocalDate.now().year),
                HttpStatus.OK
            )
                .also {
                    if (tokenService.isUserLoggedInAsSaksbehandler()) {
                        auditor.auditInternalUserRead(
                            tokenService.determineLoggedInUserId(),
                            SecurityContextUtil.getPidFromContext()
                        )
                    } else if (SecurityContextUtil.isFullmakt()) {
                        auditor.auditFullmaktRead(
                            tokenService.determineLoggedInUserId(),
                            SecurityContextUtil.getPidFromContext()
                        )
                    }
                }
        } catch (exception: Exception) {
            throw ErrorHandler.exceptionToErrorResponse(exception)
        }
    }

    @GetMapping("inntekter")
    fun getInntekter(
        @RequestParam("simuleringsaar", required = true) simuleringsaar: Int,
        @RequestParam("fetchForventedeInntekter", required = false) fetchForventedeInntekter: Boolean?
    ): ResponseEntity<InntekterResponse> {
        return try {
            ResponseEntity(
                inntektsPlanleggerService.hentInntekter(
                    SecurityContextUtil.getPidFromContext(),
                    simuleringsaar,
                    fetchForventedeInntekter?:true
                ), HttpStatus.OK
            )
        } catch (exception: Exception) {
            throw ErrorHandler.exceptionToErrorResponse(exception)
        }
    }

    @GetMapping("inntekter-for-aar")
    fun getInntekter(
        @RequestParam("aar", required = true) aar: Int,
    ): ResponseEntity<InntekterResponse> {
        return try {
            ResponseEntity(
                inntektsPlanleggerService.hentAarligeInntekter(
                    SecurityContextUtil.getPidFromContext(),
                    aar,
                ), HttpStatus.OK
            )
        } catch (exception: Exception) {
            throw ErrorHandler.exceptionToErrorResponse(exception)
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
            throw ErrorHandler.exceptionToErrorResponse(exception)
        }
    }

    @PostMapping("send")
    fun send(
        @RequestParam("simuleringsaar", required = true) simuleringsaar: Int,
        @RequestBody forventedeInntekter: ForventedeInntekter
    ): ResponseEntity<InntektsplanleggerenSendResponse> {
        try {
            return ResponseEntity(
                inntektsPlanleggerService.sendInntektsendring(
                    SecurityContextUtil.getPidFromContext(),
                    simuleringsaar,
                    forventedeInntekter
                ), HttpStatus.OK
            )
                .also {
                    if (tokenService.isUserLoggedInAsSaksbehandler()) {
                        auditor.auditInternalUserCreate(
                            tokenService.determineLoggedInUserId(),
                            SecurityContextUtil.getPidFromContext()
                        )
                    } else if (SecurityContextUtil.isFullmakt()) {
                        auditor.auditFullmaktCreate(
                            tokenService.determineLoggedInUserId(),
                            SecurityContextUtil.getPidFromContext()
                        )
                    }
                }
        } catch (exception: Exception) {
            throw ErrorHandler.exceptionToErrorResponse(exception)
        }
    }

    @GetMapping("status")
    fun getStatus(
        @RequestParam("valgtaar", required = true) valgtAr: Int,
        @RequestParam("innsendingstidspunkt", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") innsendingsTidspunkt: LocalDateTime,
    ): ResponseEntity<InntektsplanleggerenStatusResponse> {
        return try {
            ResponseEntity(
                inntektsPlanleggerService.hentStatus(
                    SecurityContextUtil.getPidFromContext(),
                    valgtAr,
                    innsendingsTidspunkt.minusSeconds(3)//juster tidspunkt noen sekunder tilbake så vi er sikker på å få med alt
                ), HttpStatus.OK
            )
        } catch (exception: Exception) {
            throw ErrorHandler.exceptionToErrorResponse(exception)
        }
    }
}