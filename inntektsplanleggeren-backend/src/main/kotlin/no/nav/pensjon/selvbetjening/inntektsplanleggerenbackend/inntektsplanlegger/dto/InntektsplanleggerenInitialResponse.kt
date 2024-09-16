package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.dto

data class InntektsplanleggerenInitialResponse(
    val messages: List<InntektsplanleggerMessage>,
    val data: InntektsplanleggerenInitialData?
)


data class InntektsplanleggerenInitialData(
    val forventetInntekt: Int,
    val forventetInntektAnnenForelder: Int? = null,
    val inntektsgrense: Int,
    val kompensasjonsgrad: Double,
    val grenseStoppAvUfoeretrygd: Int,
    val aktuelleAar: List<Int>,
    val hasVarigTilrettelagtArbeid: Boolean,
    val hasBarneTillegg: Boolean,
    val hasGjenlevendeTillegg: Boolean
)
