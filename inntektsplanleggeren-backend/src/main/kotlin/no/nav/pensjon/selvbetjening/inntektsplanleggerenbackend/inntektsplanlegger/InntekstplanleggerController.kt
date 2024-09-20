package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.dto.InntektsplanleggerenInitialResponse
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.service.InntektsplanleggerService
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security.SecurityContextUtil
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api")
class InntektsplanleggerController(
    private val inntektsPlanleggerService: InntektsplanleggerService
) {

    @GetMapping("initiate")
    fun getInntektsplanleggerenInitialData(): ResponseEntity<InntektsplanleggerenInitialResponse>  {
        return try {
            ResponseEntity(inntektsPlanleggerService.constructInitialInntektsplanleggerResponse(SecurityContextUtil.getPidFromContext()), HttpStatus.OK)
        } catch (exception: Exception) {
            throw ErrorHandler.exceptionToErrorResponse(exception,SecurityContextUtil.getPidFromContext())
        }
    }
}