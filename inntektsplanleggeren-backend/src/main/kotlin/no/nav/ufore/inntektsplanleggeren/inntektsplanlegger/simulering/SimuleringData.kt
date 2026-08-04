package no.nav.ufore.inntektsplanleggeren.inntektsplanlegger.simulering

import no.nav.ufore.inntektsplanleggeren.inntektsplanlegger.validation.InntektsplanleggerMessage

data class SimuleringData(
    val valideringsresultat: List<InntektsplanleggerMessage>,
    val simuleringsresultat: Simuleringsresultat
)
