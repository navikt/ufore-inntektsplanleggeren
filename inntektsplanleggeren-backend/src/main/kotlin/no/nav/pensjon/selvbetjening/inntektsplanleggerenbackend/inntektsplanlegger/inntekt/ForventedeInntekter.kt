package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.inntekt

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.Inntektsgrunnlag
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.InntektsgrunnlagType
import java.time.*

data class  ForventedeInntekter(val bruker: PersonInntekter, val eps: PersonInntekter?)

data class PersonInntekter(
    val arbeidsinntekt: Int?,
    val andrePensjonsgivendeYtelser: Int?,
    val naeringsinntekt: Int?,
    val inntektUtland: Int?,
    val pensjonUtland: Int?
) {
    fun sum(): Int =
        listOfNotNull(arbeidsinntekt, andrePensjonsgivendeYtelser, naeringsinntekt, inntektUtland, pensjonUtland).sum()

    fun sumInntekterRelevantTowardsInntektstak(): Int =
        listOfNotNull(arbeidsinntekt, naeringsinntekt, inntektUtland).sum()

    fun mapToInntektsgrunnlag(simuleringsaar: Int, opprettetAv: String): List<Inntektsgrunnlag> = listOf(
        createInntektsgrunnlag(InntektsgrunnlagType.ARBEIDSINNTEKT.code, arbeidsinntekt, simuleringsaar, opprettetAv),
        createInntektsgrunnlag(InntektsgrunnlagType.NAERINGSINNTEKT.code, naeringsinntekt, simuleringsaar, opprettetAv),
        createInntektsgrunnlag(InntektsgrunnlagType.ANDRE_YTELSER.code, andrePensjonsgivendeYtelser, simuleringsaar, opprettetAv),
        createInntektsgrunnlag(InntektsgrunnlagType.INNTEKT_UTLAND.code, inntektUtland, simuleringsaar, opprettetAv),
        createInntektsgrunnlag(InntektsgrunnlagType.PENSJON_UTLAND.code, pensjonUtland, simuleringsaar, opprettetAv)
    )

    private fun createInntektsgrunnlag(type: String, belop: Int?, simuleringsaar: Int, opprettetAv: String): Inntektsgrunnlag =
        Inntektsgrunnlag(
            inntektsgrunnlagId = null,
            fomDato = OffsetDateTime.ofInstant(
                ZonedDateTime.of(
                    simuleringsaar,
                    Month.JANUARY.value,
                    1,
                    0,
                    0,
                    0,
                    0,
                    ZoneId.of("Europe/Oslo")
                ).toInstant(), ZoneId.of("Europe/Oslo")
            ),
            tomDato = OffsetDateTime.ofInstant(
                ZonedDateTime.of(
                    simuleringsaar,
                    Month.DECEMBER.value,
                    31,
                    23,
                    59,
                    59,
                    0,
                    ZoneId.of("Europe/Oslo")
                ).toInstant(), ZoneId.of("Europe/Oslo")
            ),
            endringstidspunkt = OffsetDateTime.ofInstant(ZonedDateTime.now().toInstant(), ZoneId.of("Europe/Oslo")),
            belop = belop ?: 0,
            bruk = true,
            kopiertFraGammeltKrav = false,
            registerOpprettetAv = opprettetAv,
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
