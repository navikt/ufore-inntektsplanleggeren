package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.service


import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.dto.InntektsplanleggerenInitialResponse
import org.springframework.stereotype.Service

@Service
class InntektsplanleggerService {

    fun getInntektsplanleggerInitialResponse(pid: String): InntektsplanleggerenInitialResponse {

        val dummyForventetInntekt = 100000
        val dummyForventetInntektAnnenForelder = null
        val dummyInntektsgrense = 50000
        val dummyKompensasjonsgrad = 60.14
        val dummyGrenseStoppAvUfoeretrygd = 600000
        val dummyAktuelleAar: List<Int> = listOf(2024, 2025)
        val dummyHarVarigTilrettelagtArbeid = false
        val dummyHarBarneTillegg = true
        val dummyGjenlevendeTillegg = true

        return InntektsplanleggerenInitialResponse(
            dummyForventetInntekt,
            dummyForventetInntektAnnenForelder,
            dummyInntektsgrense,
            dummyKompensasjonsgrad,
            dummyGrenseStoppAvUfoeretrygd,
            dummyAktuelleAar,
            dummyHarVarigTilrettelagtArbeid,
            dummyHarBarneTillegg,
            dummyGjenlevendeTillegg
            )
    }

}