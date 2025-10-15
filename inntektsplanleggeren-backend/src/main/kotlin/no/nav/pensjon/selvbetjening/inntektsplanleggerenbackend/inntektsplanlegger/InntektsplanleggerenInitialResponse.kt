package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation.InntektsplanleggerMessage

data class InntektsplanleggerenInitialResponse(
    val messages: List<InntektsplanleggerMessage>,
    val data: InntektsplanleggerenInitialData?,
    val pid: String,
)


data class InntektsplanleggerenInitialData(
    val forventetInntekt: Map<Int, Int>,
    val forventetInntektAnnenForelder: Map<Int, Int?>,
    val inntektsgrense: Int,
    val kompensasjonsgrad: Double,
    val grenseStoppAvUfoeretrygd: Int,
    val aktuelleAar: List<Int>,
    val annetRelevantAar: Int?,
    val hasVarigTilrettelagtArbeid: Boolean,
    val hasBarneTilleggFellesbarn: Boolean,
    val hasBarnetilleggSaerkullsbarn: Boolean,
    val hasGjenlevendeTillegg: Boolean
)
