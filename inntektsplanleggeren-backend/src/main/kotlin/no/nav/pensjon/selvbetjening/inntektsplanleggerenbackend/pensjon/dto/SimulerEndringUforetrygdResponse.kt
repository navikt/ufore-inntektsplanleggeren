package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto

data class SimulerEndringUforetrygdResponse(
    val containsMotregning: Boolean,
    val totalbelopNetto: Int?,
    val sumHittilUtbetaltIAr: Int?,
    val sumNettoRestArWithoutBTandET: Int?,
    val sumRettIAr: Int?,
    val sumBruttoRestArWithoutBTandET: Double?,
    val inntektstak: Int?,
    val monthlyBarnetilleggForFellesbarn: Int?,
    val monthlyBarnetilleggForSarkullsbarn: Int?,
)