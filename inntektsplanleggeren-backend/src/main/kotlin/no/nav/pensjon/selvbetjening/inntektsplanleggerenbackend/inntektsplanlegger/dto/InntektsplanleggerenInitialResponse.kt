package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.dto

data class InntektsplanleggerenInitialResponse(
    val forventetInntekt: Int,
    val inntektsgrense: Int,
    val kompensasjonsgrad: Int,
    val grenseStoppAvUfoeretrygd: Int,
    val varigTilrettelagtArbeid: Boolean,
    val aktuelleAar: List<Int>,
    val harBarneTillegg: Boolean,
    val forventetInntektAnnenForelder: Int? = null,
)
