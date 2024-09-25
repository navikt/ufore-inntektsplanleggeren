package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.dto

data class AbonnerteInntekterIdentOgPeriode(
    val ident: Aktoer,
    val spoerringPeriodeFom: String,
    val spoerringPeriodeTom: String
)