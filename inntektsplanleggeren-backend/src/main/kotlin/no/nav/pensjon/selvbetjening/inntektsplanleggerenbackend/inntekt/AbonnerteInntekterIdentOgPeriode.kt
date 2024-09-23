package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt

data class AbonnerteInntekterIdentOgPeriode(
    val ident: Aktoer,
    val spoerringPeriodeFom: String,
    val spoerringPeriodeTom: String
)