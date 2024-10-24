package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.simulering


import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.ForventedeInntekterSummary
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.inntekt.ForventedeInntekter
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.inntekt.PersonInntekter
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation.SimuleringValidator
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.PenClient
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.Inntektsgrunnlag
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.InntektsgrunnlagType
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security.TokenService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.time.*

class SimuleringServiceTest {
    private val penClient = mock(PenClient::class.java)
    private val validator = mock(SimuleringValidator::class.java)
    private val tokenService = mock(TokenService::class.java)

    private val simuleringService = SimuleringService(penClient, validator, tokenService)

    @Test
    fun `should map oppgitte inntekter to inntektsgrunnlag when calling PEN`() {
        val simuleringsaar = LocalDate.now().year
        val simuleringFomDato = LocalDate.now().plusMonths(1).withDayOfMonth(1)
        val expectedOpprettetAv = "Saksbehanndler Sak Sakbehandlersen"
        val forventedeInntekterOppgitt = ForventedeInntekter(
            bruker = PersonInntekter(
                arbeidsinntekt = 12,
                andrePensjonsgivendeYtelser = 21,
                naeringsinntekt = 44,
                inntektUtland = 78,
                pensjonUtland = 11
            ), eps = PersonInntekter(
                arbeidsinntekt = 5,
                andrePensjonsgivendeYtelser = 899,
                naeringsinntekt = 6238,
                inntektUtland = 73619,
                pensjonUtland = 476
            )
        )

        val expectedInntektsgrunnlagBruker = listOf(
            inntektsgrunnlag(simuleringsaar, InntektsgrunnlagType.ARBEIDSINNTEKT.code, 12, expectedOpprettetAv),
            inntektsgrunnlag(simuleringsaar, InntektsgrunnlagType.NAERINGSINNTEKT.code, 6238, expectedOpprettetAv),
            inntektsgrunnlag(simuleringsaar, InntektsgrunnlagType.ANDRE_YTELSER.code, 899, expectedOpprettetAv),
            inntektsgrunnlag(simuleringsaar, InntektsgrunnlagType.INNTEKT_UTLAND.code, 73619, expectedOpprettetAv),
            inntektsgrunnlag(simuleringsaar, InntektsgrunnlagType.PENSJON_UTLAND.code, 476, expectedOpprettetAv)
        )

        val expectedInntektsgrunnlagEps = listOf(
            inntektsgrunnlag(simuleringsaar, InntektsgrunnlagType.ARBEIDSINNTEKT.code, 5, expectedOpprettetAv),
            inntektsgrunnlag(simuleringsaar, InntektsgrunnlagType.NAERINGSINNTEKT.code, 44, expectedOpprettetAv),
            inntektsgrunnlag(simuleringsaar, InntektsgrunnlagType.ANDRE_YTELSER.code, 21, expectedOpprettetAv),
            inntektsgrunnlag(simuleringsaar, InntektsgrunnlagType.INNTEKT_UTLAND.code, 78, expectedOpprettetAv),
            inntektsgrunnlag(simuleringsaar, InntektsgrunnlagType.PENSJON_UTLAND.code, 11, expectedOpprettetAv)
        )

        val forventedeInntekterRegistrert = ForventedeInntekterSummary(
            mostRecentForventedeInntekterRegistrertAndBenyttet = no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.ForventedeInntekter(
                bruker = no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.PersonInntekter(
                    null,
                    null,
                    null,
                    null,
                    null
                ),
                null
            ), sumBenyttedeInntekterBruker = 0,
            sumBenyttedeInntekterEps = null
        )
//        `when`(penClient.simulerInntektsendring(PID, simuleringFomDato, ))
        `when`(tokenService.determineLoggedInUser()).thenReturn(expectedOpprettetAv)

        simuleringService.simulerInntektsendring(
            PID,
            forventedeInntekterOppgitt,
            forventedeInntekterRegistrert,
            simuleringsaar,
            simuleringFomDato
        )


        verify(penClient.simulerInntektsendring(PID, simuleringFomDato, expectedInntektsgrunnlagBruker, expectedInntektsgrunnlagEps))
    }

    @Test
    fun `should include valideringsresultat in response`() {

    }

    @Test
    fun `should map simuleringsresultat to ytelseskomponent-year-month-format`() {

    }

    @Test
    fun `should set sum yearly uforetrygd before to sumYtelseskomponenter when simuleringsaar this year`() {

    }

    @Test
    fun `should set sum yearly uforetrygd before to totalbelopNettoAr when simuleringsaar next year`() {

    }

    private fun inntektsgrunnlag(simuleringsaar: Int, type: String, belop: Int, opprettetAv: String) = Inntektsgrunnlag(
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
        belop = belop,
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

    companion object {
        private const val PID = "00000000001"
    }
}