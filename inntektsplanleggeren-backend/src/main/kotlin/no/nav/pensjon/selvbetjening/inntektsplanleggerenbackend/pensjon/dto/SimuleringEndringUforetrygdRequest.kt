package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto

import java.time.LocalDate
import java.util.*

data class SimuleringEndringUforetrygdRequest(
    val virk: LocalDate?,
    val inntektsgrunnlagListe: List<Inntektsgrunnlag>,
    val inntektsgrunnlagListeEps: List<Inntektsgrunnlag>,
)