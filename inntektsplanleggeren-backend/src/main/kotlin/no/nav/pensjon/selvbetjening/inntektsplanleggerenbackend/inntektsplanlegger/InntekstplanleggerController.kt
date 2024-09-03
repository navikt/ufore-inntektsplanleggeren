package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.dto.*
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.PensjonService
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.service.InntektsplanleggerService

@RestController
@RequestMapping("api")
class InntektsplanleggerController(
    private val inntektsPlanleggerService: InntektsplanleggerService,
    private val pensjonService: PensjonService
) {

    private val log = LoggerFactory.getLogger(InntektsplanleggerController::class.java)

    @GetMapping("initialdata")
    fun getInntektsplanleggerenInitialData(): InntektsplanleggerenInitialResponse {
        val dummyPid = "12345678901"  //FIXME
        return try {
            log.info("Henter basic data for inntektsplanleggeren")

            inntektsPlanleggerService.getInntektsplanleggerInitialResponse(dummyPid)
        } catch (exception: Exception) {
            throw ErrorHandler.exceptionToErrorResponse(exception, dummyPid)
        }
    }

    @GetMapping("loependeberegningsperioder")
    fun getLopendeBeregningsperioder(): LopendeBeregningsPerioderResponse {
        val dummyPid = "12345678901"  //FIXME
        try {
            log.info("Henter lopende beregningsperioder for inntektsplanleggeren")
            val perioder = pensjonService.getLopendeBeregningsperiode(dummyPid)
            return LopendeBeregningsPerioderResponse(perioder["iAar"]!!, perioder["nesteAar"]!!)
        } catch (exception: Exception) {
            throw ErrorHandler.exceptionToErrorResponse(exception, dummyPid)
        }
    }
}