package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.dto

data class ForventetInntekt(
    val typeInntekt: InntektsType,
    val belop: Int
)

data class InntektsplanleggerenSimulerRequest(
    val forventetEgneInntekter: List<ForventetInntekt>,
    val forventetAnnenForelderInntekter: List<ForventetInntekt>
)

enum class InntektsType {
    ARBEIDSINNTEKT,
    PENSJONSGIVENDE_YTELSER_FRA_NAV,
    NAERINGSINNTEKT,
    INNTEKT_FRA_UTLANDET,
    PENSJON_FRA_ANDRE,
    PENSJON_FRA_UTLANDET
}