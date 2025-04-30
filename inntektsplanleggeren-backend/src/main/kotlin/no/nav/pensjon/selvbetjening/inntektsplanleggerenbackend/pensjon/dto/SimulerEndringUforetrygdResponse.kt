package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto

import java.time.LocalDate

data class SimulerEndringUforetrygdResponse(
    val currentUforetrygdSummary: UforetrygdSummary,
    val simulertUforetrygdSummary: UforetrygdSummary,
    val containsMotregning: Boolean,
    val sumHittilUtbetaltIAr: Int?,
    val sumNettoRestArWithoutBTandET: Int?,
    val sumBruttoRestArWithoutBTandET: Double?,
    val inntektstak: Int?,
    val gjeldendeBeregningFom: LocalDate?,
    val isFaktoromregnetEllerManueltOverstyrt: Boolean,
    val hasOpenInntektsendringskrav: Boolean,
    val firstVedtakFom: LocalDate,
    val lastVedtakTom: LocalDate?,
    val forventedInntektBefore: Int?
)

data class UforetrygdSummary(
    val uforetrygdYtelseskomponenter: UforetrygdYtelseskomponenter,
    val totalbelopNettoAr: Double?,
    val totalbelopNetto: Int?,
    val sumYtelseskomponenter: Int?
)

data class UforetrygdYtelseskomponenter(
    val uforetrygdOrdiner: Ytelseskomponent,
    val barnetilleggFellesbarn: Ytelseskomponent?,
    val barnetilleggSaerkullsbarn: Ytelseskomponent?,
    val ektefelletillegg: Ytelseskomponent?,
    val gjenlevendetillegg: Ytelseskomponent?
)

data class Ytelseskomponent(
    val amountPerYear: Int,
    val amountPerMonth: Int
)