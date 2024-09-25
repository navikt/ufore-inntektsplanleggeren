package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.dto

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.ForventedeInntekter

data class InntekterResponse(
    val arbeidsinntektOgYtelserHittilIAar: List<AccumulatedMaanedsinntekt>,
    val pensjonFraAndreHittilIAar: List<AccumulatedMaanedsinntekt>,
    val forventedeInntekter: ForventedeInntekter,
    val uforeHeleAaret: Boolean
)

data class AccumulatedMaanedsinntekt(val maned: Int, val belop: Double?, val inntektsgivere: List<String>)