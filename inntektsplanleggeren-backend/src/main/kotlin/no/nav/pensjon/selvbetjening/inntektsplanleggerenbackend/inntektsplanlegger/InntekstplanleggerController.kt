package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.dto.*
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.service.InntektsplanleggerService

@RestController
@RequestMapping("api")
class InntektsplanleggerController (
    private val inntektsPlanleggerService: InntektsplanleggerService
) {

    private val log = LoggerFactory.getLogger(InntektsplanleggerController::class.java)

    @GetMapping("initiate")
    fun getInntektsplanleggerenInitialData(): InntektsplanleggerenInitialResponse {
        val dummyPid = "12345678901"  //FIXME
        return try {
            log.info("Henter basic data for inntektsplanleggeren")

            inntektsPlanleggerService.getInntektsplanleggerInitialResponse(dummyPid)
        } catch (exception: Exception) {
            //throw ErrorHandler.exceptionToErrorResponse(exception, dummyPid)
            throw Exception("shit just happened")  //FIXME - hvorfor gir linja over feil??
        }
    }

}