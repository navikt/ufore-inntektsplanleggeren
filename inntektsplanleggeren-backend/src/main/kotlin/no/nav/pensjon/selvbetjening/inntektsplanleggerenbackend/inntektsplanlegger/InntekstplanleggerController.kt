package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.dto.ForventedeInntekter
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.dto.InntekterResponse
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.dto.InntektsplanleggerenInitialResponse
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.dto.SimuleringResponse
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.service.InntektsplanleggerService
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security.SecurityContextUtil
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api")
class InntektsplanleggerController(
    private val inntektsPlanleggerService: InntektsplanleggerService
) {

    @GetMapping("initiate")
    fun getInntektsplanleggerenInitialData(): ResponseEntity<InntektsplanleggerenInitialResponse> {
        return try {
            ResponseEntity(
                inntektsPlanleggerService.constructInitialInntektsplanleggerResponse(SecurityContextUtil.getPidFromContext()),
                HttpStatus.OK
            )
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
}