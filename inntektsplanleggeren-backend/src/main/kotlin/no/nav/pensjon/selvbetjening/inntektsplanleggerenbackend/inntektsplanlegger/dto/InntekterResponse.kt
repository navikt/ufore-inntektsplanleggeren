package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.dto

data class InntekterResponse(
    val arbeidsinntektOgYtelserHittilIAar: List<InntektHittilIAar>,
    val pensjonFraAndreHittilIAar: List<InntektHittilIAar>,
    val forventetEgneInntekter: List<ForventetInntekt>,
    val forventetAnnenForelderInntekter: List<ForventetInntekt>
)

data class InntektHittilIAar(val maned: Int, val belop: Double?, val inntektsgiver: String?)

data class ForventetInntekt(
    val typeInntekt: Inntektstype,
    val belop: Double
)

enum class Inntektstype {
    ARBEIDSINNTEKT,
    PENSJONSGIVENDE_YTELSER_FRA_NAV,
    NAERINGSINNTEKT,
    INNTEKT_FRA_UTLANDET,
    PENSJON_FRA_ANDRE,
    PENSJON_FRA_UTLANDET
}