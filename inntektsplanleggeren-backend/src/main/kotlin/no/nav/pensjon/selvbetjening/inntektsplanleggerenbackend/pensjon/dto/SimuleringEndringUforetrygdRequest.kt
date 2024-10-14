package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto

import java.time.LocalDate

data class SimuleringEndringUforetrygdRequest(
    val pid: String,
    val virk: LocalDate?,
    val inntektsgrunnlagListe: List<Inntektsgrunnlag>,
    val inntektsgrunnlagListeEps: List<Inntektsgrunnlag>,
)