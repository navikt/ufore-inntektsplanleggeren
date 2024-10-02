package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.dto

data class ForventedeInntekter(val bruker: PersonInntekter, val eps: PersonInntekter?)

data class PersonInntekter(
    val arbeidsinntekt: Int?,
    val andrePensjonsgivendeYtelser: Int?,
    val naeringsinntekt: Int?,
    val inntektUtland: Int?,
    val pensjonUtland: Int?
) {
    fun sum(): Int =
        listOfNotNull(arbeidsinntekt, andrePensjonsgivendeYtelser, naeringsinntekt, inntektUtland, pensjonUtland).sum()
}
