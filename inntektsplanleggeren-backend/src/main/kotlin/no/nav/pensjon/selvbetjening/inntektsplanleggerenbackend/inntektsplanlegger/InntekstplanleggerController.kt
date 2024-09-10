package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

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

    @GetMapping("loepende-beregningsperioder")
    fun getLopendeBeregningsperioder(): ResponseEntity<Any> {
        val dummyPid = "26853949913"  //FIXME
        try {
            log.info("Henter lopende beregningsperioder for inntektsplanleggeren")
            val perioder = pensjonService.getLopendeBeregningsperiode(dummyPid)
            val tmp = LopendeBeregningsPerioderResponse(perioder["iAar"]!!, perioder["nesteAar"]!!)
            //val responseEntity = ResponseEntity<Any>(tmp, HttpStatus.OK)
            return ResponseEntity(tmp, HttpStatus.OK)
        } catch (exception: Exception) {
            throw ErrorHandler.exceptionToErrorResponse(exception, dummyPid)
        }
    }

    @GetMapping("initiate")
    fun getInntektsplanleggerenInitialData(): ResponseEntity<Any>  {
        val dummyPid = "26853949913"  //FIXME
        return try {
            log.info("Henter basic data for inntektsplanleggeren")

            ResponseEntity(inntektsPlanleggerService.getInntektsplanleggerInitialResponse(dummyPid), HttpStatus.OK)
        } catch (exception: Exception) {
            throw ErrorHandler.exceptionToErrorResponse(exception, dummyPid)
        }
    }


}