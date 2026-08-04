package no.nav.ufore.inntektsplanleggeren.inntekt.model

import no.nav.ufore.inntektsplanleggeren.inntekt.dto.Inntektshendelse

data class ForventedeInntekterSummary(
    val mostRecentForventedeInntekterRegistrertAndBenyttet: ForventedeInntekter,
    val sumBenyttedeInntekterBruker: Int,
    val sumBenyttedeInntekterEps: Int?
)

data class ForventedeInntekter(val bruker: PersonInntekter, val eps: PersonInntekter?) {
    fun toDto(): no.nav.ufore.inntektsplanleggeren.inntektsplanlegger.inntekt.ForventedeInntekter =
        no.nav.ufore.inntektsplanleggeren.inntektsplanlegger.inntekt.ForventedeInntekter(
            no.nav.ufore.inntektsplanleggeren.inntektsplanlegger.inntekt.PersonInntekter(
                arbeidsinntekt = this.bruker.arbeidsinntekt?.belop,
                andrePensjonsgivendeYtelser = this.bruker.andrePensjonsgivendeYtelser?.belop,
                naeringsinntekt = this.bruker.naeringsinntekt?.belop,
                inntektUtland = this.bruker.inntektUtland?.belop,
                pensjonUtland = this.bruker.pensjonUtland?.belop
            ),
            if (eps != null) {
                no.nav.ufore.inntektsplanleggeren.inntektsplanlegger.inntekt.PersonInntekter(
                    arbeidsinntekt = this.eps.arbeidsinntekt?.belop,
                    andrePensjonsgivendeYtelser = this.eps.andrePensjonsgivendeYtelser?.belop,
                    naeringsinntekt = this.eps.naeringsinntekt?.belop,
                    inntektUtland = this.eps.inntektUtland?.belop,
                    pensjonUtland = this.eps.pensjonUtland?.belop
                )
            } else null
        )
}

data class PersonInntekter(
    val arbeidsinntekt: Personinntekt?,
    val andrePensjonsgivendeYtelser: Personinntekt?,
    val naeringsinntekt: Personinntekt?,
    val inntektUtland: Personinntekt?,
    val pensjonUtland: Personinntekt?
)

data class Personinntekt(val belop: Int, val status: Inntektshendelse?)
