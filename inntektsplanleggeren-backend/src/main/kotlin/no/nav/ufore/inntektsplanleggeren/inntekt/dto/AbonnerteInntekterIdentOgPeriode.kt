package no.nav.ufore.inntektsplanleggeren.inntekt.dto

data class AbonnerteInntekterIdentOgPeriode(
    val ident: Aktoer,
    val spoerringPeriodeFom: String,
    val spoerringPeriodeTom: String
)