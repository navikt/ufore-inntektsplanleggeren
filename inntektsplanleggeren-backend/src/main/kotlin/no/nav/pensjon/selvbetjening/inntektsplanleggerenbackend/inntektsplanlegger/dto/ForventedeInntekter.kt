package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.dto

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.Inntektsgrunnlag
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.InntektsgrunnlagType
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security.SecurityContextUtil
import java.time.LocalDate
import java.time.Month

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

    fun mapToInntektsgrunnlag(simuleringsaar: Int): List<Inntektsgrunnlag> = listOf(
        createInntektsgrunnlag(InntektsgrunnlagType.ARBEIDSINNTEKT.code, arbeidsinntekt, simuleringsaar),
        createInntektsgrunnlag(InntektsgrunnlagType.NAERINGSINNTEKT.code, naeringsinntekt, simuleringsaar),
        createInntektsgrunnlag(InntektsgrunnlagType.ANDRE_YTELSER.code, andrePensjonsgivendeYtelser, simuleringsaar),
        createInntektsgrunnlag(InntektsgrunnlagType.INNTEKT_UTLAND.code, inntektUtland, simuleringsaar),
        createInntektsgrunnlag(InntektsgrunnlagType.PENSJON_UTLAND.code, pensjonUtland, simuleringsaar)
    )

    private fun createInntektsgrunnlag(type: String, belop: Int?, simuleringsaar: Int): Inntektsgrunnlag = Inntektsgrunnlag(
        inntektsgrunnlagId = null,
        fomDato = LocalDate.of(simuleringsaar, Month.JANUARY, 1),
        tomDato = LocalDate.of(simuleringsaar, Month.DECEMBER, 31),
        endringstidspunkt = LocalDate.now(),
        belop = belop ?: 0,
        bruk = true,
        kopiertFraGammeltKrav = false,
        registerOpprettetAv = "", //TODO
        grunnlagKilde = "BRUKER_OPP",
        registerKilde = "SELVBETJ",
        inntektType = type,
        inntektHendelseType = "BENYTTET",
        grunnIkkeReduksjonType = null,
        persongrunnlagId = null,
        changeStamp = null,
        version = null
    )
}
