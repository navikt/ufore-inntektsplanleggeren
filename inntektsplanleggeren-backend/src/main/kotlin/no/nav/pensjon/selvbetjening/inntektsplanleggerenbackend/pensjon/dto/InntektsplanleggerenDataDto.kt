package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.planlegger

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.planlegger.*
import com.fasterxml.jackson.annotation.JsonProperty
//import java.time.LocalDate
//import no.stelvio.domain.person.Pid
import java.util.Date

//TODO: What to do?
data class Pid (
   val pid: String
)
data class PidDto (
    @JsonProperty("pid") val pid: String
) {
    fun toIdent(): String = this.pid
}

//TODO: Dummy class
data class Inntektsgrunnlag (
    @JsonProperty("tmp") val tmp: Boolean
)

//TODO: Dummy class
data class FaktiskInntektFormData(
    @JsonProperty("tmp") val tmp: Boolean
)

data class InntektsplanleggerenDataDto(
    @JsonProperty("inntektsgrunnlagMap") val inntektsgrunnlagMap: Map<String, List<Inntektsgrunnlag>?>,
    @JsonProperty("hasOpenKrav") val hasOpenKrav: Boolean,
    @JsonProperty("hasOpenInntektsendringskrav") val hasOpenInntektsendringskrav: Boolean,
    @JsonProperty("epsData") val epsData: Pid?,
    @JsonProperty("barnetilleggMap") val barnetilleggMap: Map<String, Boolean?>,
    @JsonProperty("gjelderFomBeregningsperiode") val gjelderFomBeregningsperiode: Date?,
    @JsonProperty("rettIArTotal") val rettIArTotal: Int?,
    @JsonProperty("totalbelopNettoAr") val totalbelopNettoAr: Int?,
    @JsonProperty("totalbelopNetto") val totalbelopNetto: Int?,
    @JsonProperty("vedtakId") val vedtakId: Long?,
    @JsonProperty("sakId") val sakId: Long?,
    @JsonProperty("isFaktoromregnetEllerManueltOverstyrt") val isFaktoromregnetEllerManueltOverstyrt: Boolean,
    @JsonProperty("vedtakDatoMap") val vedtakDatoMap: Map<String, Date?>,
    @JsonProperty("faktiskInntektFormDataMap") val faktiskInntektFormDataMap: Map<String, List<FaktiskInntektFormData>?>,
    @JsonProperty("sumFaktiskeInntekerEpsMap") val sumFaktiskeInntekerEpsMap: Map<String, Double>,
    @JsonProperty("hasErrorMessageFromEdag") val hasErrorMessageFromEdag: Boolean
){
    fun toInntektsplanleggerenData(): InntektsplanleggerenData = InntektsplanleggerenData(
        this.inntektsgrunnlagMap,
        this.hasOpenKrav,
        this.hasOpenInntektsendringskrav,
        this.epsData?.pid,
        this.barnetilleggMap,
        this.gjelderFomBeregningsperiode,
        this.rettIArTotal,
        this.totalbelopNettoAr,
        this.totalbelopNetto,
        this.vedtakId,
        this.sakId,
        this.isFaktoromregnetEllerManueltOverstyrt,
        this.vedtakDatoMap,
        this.faktiskInntektFormDataMap,
        this.sumFaktiskeInntekerEpsMap,
        this.hasErrorMessageFromEdag
    )
}

data class SimuleringEndringUforetrygdRequestDto(
    @JsonProperty("virk") val virk: Date?,
    @JsonProperty("inntektsgrunnlagListe") val inntektsgrunnlagListe: List<Inntektsgrunnlag>,
    @JsonProperty("inntektsgrunnlagListeEps") val inntektsgrunnlagListeEps: List<Inntektsgrunnlag>,
){
    fun toSimuleringEndringUforetrygdRequest(): SimuleringEndringUforetrygdRequest = SimuleringEndringUforetrygdRequest(
        this.virk,
        this.inntektsgrunnlagListe,
        this.inntektsgrunnlagListeEps
    )
}

data class SimuleringEndringUforetrygdResponseDto(
    @JsonProperty("containsMotregning") val containsMotregning: Boolean,
    @JsonProperty("totalbelopNetto") val totalbelopNetto: Int?,
    @JsonProperty("sumHittilUtbetaltIAr") val sumHittilUtbetaltIAr: Int?,
    @JsonProperty("sumNettoRestArWithoutBTandET") val sumNettoRestArWithoutBTandET: Int?,
    @JsonProperty("sumRettIAr") val sumRettIAr: Int?,
    @JsonProperty("sumBruttoRestArWithoutBTandET") val sumBruttoRestArWithoutBTandET: Double?,
    @JsonProperty("inntektstak") val inntektstak: Int?,
    @JsonProperty("monthlyBarnetilleggForFellesbarn") val monthlyBarnetilleggForFellesbarn: Int?,
    @JsonProperty("monthlyBarnetilleggForSarkullsbarn") val monthlyBarnetilleggForSarkullsbarn: Int?,
) {
    fun toSimuleringEndringUforetrygdResponse(): SimuleringEndringUforetrygdResponse = SimuleringEndringUforetrygdResponse (
        this.containsMotregning,
        this.totalbelopNetto,
        this.sumHittilUtbetaltIAr,
        this.sumNettoRestArWithoutBTandET,
        this.sumRettIAr,
        this.sumBruttoRestArWithoutBTandET,
        this.inntektstak,
        this.monthlyBarnetilleggForFellesbarn,
        this.monthlyBarnetilleggForSarkullsbarn
    )
}

data class InntektsplanleggerenForventedeInntekterDto(
    @JsonProperty("arbeidsinntekt") var arbeidsinntekt: Int? = null,
    @JsonProperty("naeringsinntekt") var naeringsinntekt: Int? = null,
    @JsonProperty("utlandsinntekt") var utlandsinntekt: Int? = null,
    @JsonProperty("pensjonFraUtlandet") var pensjonFraUtlandet: Int? = null,
    @JsonProperty("andreYtelser") var andreYtelser: Int? = null,
) {
    fun toInntektsplanleggerenForventedeInntekter(): InntektsplanleggerenForventedeInntekter = InntektsplanleggerenForventedeInntekter(
        this.arbeidsinntekt,
        this.naeringsinntekt,
        this.utlandsinntekt,
        this.pensjonFraUtlandet,
        this.andreYtelser
    )
}

data class VedtakDatoDataDto(
    @JsonProperty("gjelderFom") val gjelderFom: Date?,
    @JsonProperty("gjelderTom") val gjelderTom: Date?,
    @JsonProperty("isUforetrygd") val isUforetrygd: Boolean,
) {
    fun toVedtakDatoData(): VedtakDatoData = VedtakDatoData(
        this.gjelderFom,
        this.gjelderTom,
        this.isUforetrygd
    )
}

class VurderNyttVedtakForSimulertBeregningRequestDto(
    @JsonProperty("simulertTotalbelopNetto") val simulertTotalbelopNetto: Int,
    @JsonProperty("eksisterendeTotalbelopNetto") val eksisterendeTotalbelopNetto: Int,
    @JsonProperty("fnr") val fnr: String,
    @JsonProperty("fnrEps") val fnrEps: Pid, //TODO: Sjekk nullpointer
    @JsonProperty("forventetInntektBrukerList") val forventetInntektBrukerList: List<Inntektsgrunnlag>,
    @JsonProperty("forventetInntektEpsList") val forventetInntektEpsList: List<Inntektsgrunnlag>,
    @JsonProperty("kravVirkFom") val kravVirkFom: Date,
    @JsonProperty("sakId") val sakId: Long?,
    @JsonProperty("isSelvbetjeningsSone") val isSelvbetjeningsSone: Boolean
) {
    fun toVurderNyttVedtakForSimulertBeregningRequest(): VurderNyttVedtakForSimulertBeregningRequest = VurderNyttVedtakForSimulertBeregningRequest(
        this.simulertTotalbelopNetto,
        this.eksisterendeTotalbelopNetto,
        this.fnr,
        this.fnrEps.pid,
        this.forventetInntektBrukerList,
        this.forventetInntektEpsList,
        this.kravVirkFom,
        this.sakId,
        this.isSelvbetjeningsSone
    )
}


