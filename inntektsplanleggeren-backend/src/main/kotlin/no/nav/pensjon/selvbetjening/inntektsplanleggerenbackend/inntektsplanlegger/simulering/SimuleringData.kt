package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.simulering

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation.InntektsplanleggerMessage

data class SimuleringData(
    val valideringsresultat: List<InntektsplanleggerMessage>,
    val simuleringsresultat: Simuleringsresultat
)
