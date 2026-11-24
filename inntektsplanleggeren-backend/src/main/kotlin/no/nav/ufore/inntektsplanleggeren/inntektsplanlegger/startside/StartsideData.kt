package no.nav.ufore.inntektsplanleggeren.inntektsplanlegger.startside

import no.nav.ufore.inntektsplanleggeren.inntektsplanlegger.validation.InntektsplanleggerMessage

data class StartsideData(
    val messages: List<InntektsplanleggerMessage>,
    val uforetrygdIAar: UføretrygdOversikt?,
    val uforetrygdNesteAar: UføretrygdOversikt?,
    val aarKanRegistrereInntekt: List<Int>,
    val seTallForAar: Int?,
)

data class UføretrygdOversikt(
    val forAar: Int,
    val forventetInntekt: Int,
    val forventetInntektAnnenForelder: Int,
    val bunnfradrag: Int,
    val reduksjonsprosent: Double,
    val inntektstak: Int,
    val harBarnetilleggFellesbarn: Boolean,
)
