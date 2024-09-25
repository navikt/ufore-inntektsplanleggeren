package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.dto.Inntektshendelse

data class ForventedeInntekter(val bruker: PersonInntekter, val eps: PersonInntekter?)

data class PersonInntekter(
    val arbeidsinntekt: Personinntekt?,
    val andrePensjonsgivendeYtelser: Personinntekt?,
    val naeringsinntekt: Personinntekt?,
    val inntektUtland: Personinntekt?,
    val pensjonUtland: Personinntekt?
)

data class Personinntekt(val belop: Int, val status: Inntektshendelse?)
