package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.dto.*
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.PensjonService
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security.SecurityContextUtil
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.service.InntektsplanleggerService

@RestController
@RequestMapping("api")
class InntektsplanleggerController(
    private val inntektsPlanleggerService: InntektsplanleggerService,
    private val pensjonService: PensjonService
) {

    private val log = LoggerFactory.getLogger(InntektsplanleggerController::class.java)

    @GetMapping("loepende-beregningsperioder")
    fun getLopendeBeregningsperioder(): ResponseEntity<Any> {
        try {
            log.info("Henter lopende beregningsperioder for inntektsplanleggeren")
            val perioder = pensjonService.getLopendeBeregningsperiode(SecurityContextUtil.getPidFromContext())
            return ResponseEntity(LopendeBeregningsPerioderResponse(perioder["iAar"]!!, perioder["nesteAar"]!!), HttpStatus.OK)
        } catch (exception: Exception) {
            throw ErrorHandler.exceptionToErrorResponse(exception, SecurityContextUtil.getPidFromContext())
        }
    }

    @GetMapping("initiate")
    fun getInntektsplanleggerenInitialData(): ResponseEntity<Any>  {
        return try {
            log.info("Henter initielle data for inntektsplanleggeren")

            ResponseEntity(inntektsPlanleggerService.getInntektsplanleggerInitialResponse(SecurityContextUtil.getPidFromContext()), HttpStatus.OK)
        } catch (exception: Exception) {
            throw ErrorHandler.exceptionToErrorResponse(exception,SecurityContextUtil.getPidFromContext())
        }
    }


}