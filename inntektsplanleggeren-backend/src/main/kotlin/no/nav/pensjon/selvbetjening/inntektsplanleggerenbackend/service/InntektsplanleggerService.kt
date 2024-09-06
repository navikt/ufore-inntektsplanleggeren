package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.service


import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.dto.InntektsplanleggerenInitialResponse
import org.springframework.stereotype.Service

@Service
class InntektsplanleggerService {

    fun getInntektsplanleggerInitialResponse(pid: String): InntektsplanleggerenInitialResponse {
        val dummyInntektsgrense = 50000 // FIXME
        val dummyForventetInntekt = 100000
        val dummyKompensasjonsgrad = 60
        val dummyGrenseStoppAvUfoeretrygd = 600000
        val dummyVarigTilrettelagtArbeid = false
        val dummyAktuelleAar: List<Int> = listOf(2024)
        val dummyHarBarneTillegg = true
        val dummyForventetInntektAnnenForelder = null

        return InntektsplanleggerenInitialResponse(
            dummyInntektsgrense,
            dummyForventetInntekt,
            dummyKompensasjonsgrad,
            dummyGrenseStoppAvUfoeretrygd,
            dummyVarigTilrettelagtArbeid,
            dummyAktuelleAar,
            dummyHarBarneTillegg,
            dummyForventetInntektAnnenForelder
            )
    }

}