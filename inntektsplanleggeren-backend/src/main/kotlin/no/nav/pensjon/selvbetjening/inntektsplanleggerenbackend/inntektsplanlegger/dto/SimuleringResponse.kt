package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.dto

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation.InntektsplanleggerMessage

data class SimuleringResponse(val messages: List<InntektsplanleggerMessage>,
                              val data: Simuleringsresultat?)

data class Simuleringsresultat(val temp: String)
