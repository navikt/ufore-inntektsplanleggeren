package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto

import java.time.LocalDate

data class InnsendingRequest(
    val pid: String,
    val simulertTotalbelopNetto: Int,
    val virkFom: LocalDate,
    val forventetInntektBruker: List<Inntektsgrunnlag>,
    val forventetInntektEps: List<Inntektsgrunnlag>,
    val innsendtAv: String)

data class InnsendingResponse(val status: String)

enum class BehandlingStatus{
    INNTEKT_LAGRET,
    AUTOMATISK_BEHANDLING
}