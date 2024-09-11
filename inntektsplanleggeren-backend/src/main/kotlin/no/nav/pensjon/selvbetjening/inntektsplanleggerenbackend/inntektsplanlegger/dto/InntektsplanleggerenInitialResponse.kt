package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.dto

data class InntektsplanleggerenInitialResponse(
    val forventetInntekt: Int,
    val forventetInntektAnnenForelder: Int? = null,
    val inntektsgrense: Int,
    val kompensasjonsgrad: Double,
    val grenseStoppAvUfoeretrygd: Int,
    val aktuelleAar: List<Int>,
    val harVarigTilrettelagtArbeid: Boolean,
    val harBarneTillegg: Boolean,
    val harGjenlevendeTillegg: Boolean
)
