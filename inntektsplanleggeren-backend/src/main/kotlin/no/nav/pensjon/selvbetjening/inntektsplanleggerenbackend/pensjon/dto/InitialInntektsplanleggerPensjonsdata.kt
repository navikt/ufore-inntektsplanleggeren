package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto

data class InitialInntektsplanleggerPensjonsdata(
    val forventetInntekt: Int,
    val forventetInntektAnnenForelder: Int? = null,
    val inntektsgrense: Int,
    val kompensasjonsgrad: Double,
    val grenseStoppAvUfoeretrygd: Int,
    val hasLopendeUforeVedtakThisYear: Boolean,
    val hasLopendeUforeVedtakNextYear: Boolean,
    val hasVarigTilrettelagtArbeid: Boolean,
    val hasBarneTillegg: Boolean,
    val hasGjenlevendeTillegg: Boolean
)
