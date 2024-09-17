package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.InitialInntektsplanleggerPensjonsdata
import org.springframework.stereotype.Service

@Service
class Validator {
    fun validateUserInitialData(initialPensjonsdata: InitialInntektsplanleggerPensjonsdata?): List<InntektsplanleggerMessage> {
        if (initialPensjonsdata == null) {
            return listOf(InntektsplanleggerMessage(InntektsplanleggerMessageCode.USER_HAS_NO_UFORE))
        }

        if (!initialPensjonsdata.hasLopendeUforeVedtakNextYear && !initialPensjonsdata.hasLopendeUforeVedtakThisYear) {
            return listOf(InntektsplanleggerMessage(InntektsplanleggerMessageCode.USER_HAS_NO_LOPENDE_VEDTAK_YET))
        }
        return emptyList()
    }
}