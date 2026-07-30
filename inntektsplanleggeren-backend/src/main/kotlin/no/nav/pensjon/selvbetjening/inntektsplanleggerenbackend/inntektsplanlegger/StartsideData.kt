package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation.InntektsplanleggerMessage

data class StartsideData(
    val messages: List<InntektsplanleggerMessage>,
    val uforetrygd: UføretrygdOversikt?,
)


data class UføretrygdOversikt(
    val forventetInntekt: Map<Int, Int>,
    val forventetInntektAnnenForelder: Map<Int, Int?>,
    val inntektsgrense: Int,
    val reduksjonsprosent: Double,
    val inntektstak: Int,
    val harBarnetilleggFellesbarn: Boolean,
    val aarKanRegistrereInntekt: List<Int>,
    val seTallForAar: Int?,
)
