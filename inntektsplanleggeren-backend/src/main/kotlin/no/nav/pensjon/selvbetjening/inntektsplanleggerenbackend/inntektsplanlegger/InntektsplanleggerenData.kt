package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.planlegger

import java.util.*

data class InntektsplanleggerenData(
    val inntektsgrunnlagMap: Map<String, List<Inntektsgrunnlag>?>,
    val hasOpenKrav: Boolean,
    val hasOpenInntektsendringskrav: Boolean,
    val epsData: String?,                           // Changed from Pid
    val barnetilleggMap: Map<String, Boolean?>,
    val gjelderFomBeregningsperiode: Date?,
    val rettIArTotal: Int?,
    val totalbelopNettoAr: Int?,
    val totalbelopNetto: Int?,
    val vedtakId: Long?,
    val sakId: Long?,
    val isFaktoromregnetEllerManueltOverstyrt: Boolean,
    val vedtakDatoMap: Map<String, Date?>,
    val faktiskInntektFormDataMap: Map<String, List<FaktiskInntektFormData>?>,
    val sumFaktiskeInntekerEpsMap: Map<String, Double>,
    val hasErrorMessageFromEdag: Boolean,
)

data class SimuleringEndringUforetrygdRequest(
    val virk: Date?,
    val inntektsgrunnlagListe: List<Inntektsgrunnlag>,
    val inntektsgrunnlagListeEps: List<Inntektsgrunnlag>,
)

data class SimuleringEndringUforetrygdResponse(
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

data class InntektsplanleggerenForventedeInntekter(
    var arbeidsinntekt: Int? = null,
    var naeringsinntekt: Int? = null,
    var utlandsinntekt: Int? = null,
    var pensjonFraUtlandet: Int? = null,
    var andreYtelser: Int? = null,
)

data class VedtakDatoData(
    val gjelderFom: Date?,
    val gjelderTom: Date?,
    val isUforetrygd: Boolean,
)

class VurderNyttVedtakForSimulertBeregningRequest(
    val simulertTotalbelopNetto: Int,
    val eksisterendeTotalbelopNetto: Int,
    val fnr: String,
    val fnrEps: String,                                         // Changed from Pid
    val forventetInntektBrukerList: List<Inntektsgrunnlag>,
    val forventetInntektEpsList: List<Inntektsgrunnlag>,
    val kravVirkFom: Date,
    val sakId: Long?,
    val isSelvbetjeningsSone: Boolean
)


