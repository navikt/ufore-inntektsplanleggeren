package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt

import org.springframework.stereotype.Component

@Component
class InntektskomponentClient {
    fun hentForventetInntekt(
        fnr: String,
        inntektsAar: List<Int>
    ): HentForventetInntektResponse {
        //TODO: Implement this
        return HentForventetInntektResponse(null)
    }

    fun hentAbonnerteInntekterBolk(
        abonnerteInntekterIdentOgPeriodeListe: List<AbonnerteInntekterIdentOgPeriode>,
        ainntektsfilter: String,
        formaal: String
    ): HentAbonnerteInntekterBolkResponse{
        //TODO: Implement this
        return HentAbonnerteInntekterBolkResponse(emptyList(), emptyList())
    }
}