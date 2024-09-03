package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.dto

data class LopendeBeregningsPerioderResponse(
    val harLoependeBeregningsPeriodeiAar: Boolean,
    val harLoependeBergeningsPeriodeNesteAar: Boolean
)