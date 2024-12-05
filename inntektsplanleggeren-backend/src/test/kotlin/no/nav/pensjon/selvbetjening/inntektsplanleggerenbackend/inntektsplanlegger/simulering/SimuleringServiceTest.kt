package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.simulering


import junit.framework.TestCase.assertNull
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.ForventedeInntekterSummary
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.inntekt.ForventedeInntekter
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.inntekt.PersonInntekter
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation.InntektsplanleggerMessage
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation.InntektsplanleggerMessageCode
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation.SimuleringValidator
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.PenClient
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.*
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security.TokenService
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.util.NowProvider
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.time.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class SimuleringServiceTest {
    private val penClient = mock(PenClient::class.java)
    private val validator = mock(SimuleringValidator::class.java)
    private val tokenService = mock(TokenService::class.java)
    private val nowProvider = mock(NowProvider::class.java)

    private val simuleringService = SimuleringService(penClient, validator, tokenService, nowProvider)

    @Captor
    private lateinit var captor: ArgumentCaptor<List<Inntektsgrunnlag>>

    @BeforeEach
    fun init() {
        MockitoAnnotations.openMocks(this)
        `when`(nowProvider.now()).thenReturn(LocalDate.now())
    }

    @Test
    fun `should map oppgitte inntekter to inntektsgrunnlag when calling PEN`() {
        val simuleringsaar = LocalDate.now().year
        val simuleringFomDato = LocalDate.now().plusMonths(1).withDayOfMonth(1)
        val expectedOpprettetAv = "Saksbehandler Sak Sakbehandlersen"
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
            inntektsgrunnlag(simuleringsaar, InntektsgrunnlagType.NAERINGSINNTEKT.code, 44, expectedOpprettetAv),
            inntektsgrunnlag(simuleringsaar, InntektsgrunnlagType.ANDRE_YTELSER.code, 21, expectedOpprettetAv),
            inntektsgrunnlag(simuleringsaar, InntektsgrunnlagType.INNTEKT_UTLAND.code, 78, expectedOpprettetAv),
            inntektsgrunnlag(simuleringsaar, InntektsgrunnlagType.PENSJON_UTLAND.code, 11, expectedOpprettetAv)
        )

        val expectedInntektsgrunnlagEps = listOf(
            inntektsgrunnlag(simuleringsaar, InntektsgrunnlagType.ARBEIDSINNTEKT.code, 5, expectedOpprettetAv),
            inntektsgrunnlag(simuleringsaar, InntektsgrunnlagType.NAERINGSINNTEKT.code, 6238, expectedOpprettetAv),
            inntektsgrunnlag(simuleringsaar, InntektsgrunnlagType.ANDRE_YTELSER.code, 899, expectedOpprettetAv),
            inntektsgrunnlag(simuleringsaar, InntektsgrunnlagType.INNTEKT_UTLAND.code, 73619, expectedOpprettetAv),
            inntektsgrunnlag(simuleringsaar, InntektsgrunnlagType.PENSJON_UTLAND.code, 476, expectedOpprettetAv)
        )

        val forventedeInntekterRegistrert = ForventedeInntekterSummary(
            mostRecentForventedeInntekterRegistrertAndBenyttet = no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.ForventedeInntekter(
                bruker = no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.PersonInntekter(
                    null, null, null, null, null
                ), null
            ), sumBenyttedeInntekterBruker = 0, sumBenyttedeInntekterEps = null
        )
        `when`(
            penClient.simulerInntektsendring(
                any(), any(), any(), any()
            )
        ).thenReturn(
            SimulerEndringUforetrygdResponse(
                UforetrygdSummary(
                    uforetrygdYtelseskomponenter = UforetrygdYtelseskomponenter(
                        Ytelseskomponent(0, 0), null, null, null, null
                    ), null, null, null
                ), UforetrygdSummary(
                    uforetrygdYtelseskomponenter = UforetrygdYtelseskomponenter(
                        Ytelseskomponent(0, 0), null, null, null, null
                    ), null, null, null
                ), false,
                null,
                null,

                null,
                null,
                null,
                false,
                false,
                LocalDate.now(),
                null
            )
        )
        `when`(tokenService.determineLoggedInUser()).thenReturn(expectedOpprettetAv)

        simuleringService.simulerInntektsendring(
            PID, forventedeInntekterOppgitt, forventedeInntekterRegistrert, simuleringsaar, simuleringFomDato
        )

        verify(penClient, times(1)).simulerInntektsendring(any(), any(), capture(captor), capture(captor))

        assertEquals(expectedInntektsgrunnlagBruker.size, captor.allValues[0].size)
        assertEquals(expectedInntektsgrunnlagEps.size, captor.allValues[1].size)

        for (i: Int in expectedInntektsgrunnlagBruker.indices) {
            assertInntektsgrunnlagEqual(expectedInntektsgrunnlagBruker[i], captor.allValues[0][i])
        }
        for (i: Int in expectedInntektsgrunnlagEps.indices) {
            assertInntektsgrunnlagEqual(expectedInntektsgrunnlagEps[i], captor.allValues[1][i])
        }
    }

    @Test
    fun `should include valideringsresultat in response`() {
        val simuleringsaar = LocalDate.now().year
        val simuleringFomDato = LocalDate.now().plusMonths(1).withDayOfMonth(1)
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

        val forventedeInntekterRegistrert = ForventedeInntekterSummary(
            mostRecentForventedeInntekterRegistrertAndBenyttet = no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.ForventedeInntekter(
                bruker = no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.PersonInntekter(
                    null, null, null, null, null
                ), null
            ), sumBenyttedeInntekterBruker = 0, sumBenyttedeInntekterEps = null
        )
        val expectedSimuleringsresultat = SimulerEndringUforetrygdResponse(
            UforetrygdSummary(
                uforetrygdYtelseskomponenter = UforetrygdYtelseskomponenter(
                    Ytelseskomponent(0, 0), null, null, null, null
                ), null, null, null
            ), UforetrygdSummary(
                uforetrygdYtelseskomponenter = UforetrygdYtelseskomponenter(
                    Ytelseskomponent(0, 0), null, null, null, null
                ), null, null, null
            ), false,
            null,
            null,

            null,
            null,
            null,
            false,
            false,
            LocalDate.now(),
            null
        )
        `when`(penClient.simulerInntektsendring(any(), any(), any(), any())).thenReturn(expectedSimuleringsresultat)
        `when`(tokenService.determineLoggedInUser()).thenReturn("Saksbehandler Sak Sakbehandlersen")

        val expectedValideringsresultat =
            listOf(InntektsplanleggerMessage(InntektsplanleggerMessageCode.SIMULERING_CONTAINS_MOTREGNING))
        `when`(
            validator.validateSimulering(
                expectedSimuleringsresultat,
                forventedeInntekterOppgitt,
                simuleringFomDato,
                simuleringsaar,
                PID
            )
        ).thenReturn(expectedValideringsresultat)

        val result = simuleringService.simulerInntektsendring(
            PID, forventedeInntekterOppgitt, forventedeInntekterRegistrert, simuleringsaar, simuleringFomDato
        )

        assertEquals(expectedValideringsresultat, result.valideringsresultat)
    }

    @Test
    fun `should map simuleringsresultat to ytelseskomponent-year-month-format`() {
        val simuleringsaar = LocalDate.now().year
        val simuleringFomDato = LocalDate.now().plusMonths(1).withDayOfMonth(1)
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

        val forventedeInntekterRegistrert = ForventedeInntekterSummary(
            mostRecentForventedeInntekterRegistrertAndBenyttet = no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.ForventedeInntekter(
                bruker = no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.PersonInntekter(
                    null, null, null, null, null
                ), null
            ), sumBenyttedeInntekterBruker = 400000, sumBenyttedeInntekterEps = null
        )
        val expectedSimuleringsresultat = SimulerEndringUforetrygdResponse(
            UforetrygdSummary(
                uforetrygdYtelseskomponenter = UforetrygdYtelseskomponenter(
                    uforetrygdOrdiner = Ytelseskomponent(33, 56),
                    barnetilleggFellesbarn = Ytelseskomponent(30, 275),
                    barnetilleggSaerkullsbarn = Ytelseskomponent(434, 1094),
                    gjenlevendetillegg = Ytelseskomponent(382, 43),
                    ektefelletillegg = Ytelseskomponent(2234, 5039)
                ), 400000.0, 35000, 430000
            ), UforetrygdSummary(
                uforetrygdYtelseskomponenter = UforetrygdYtelseskomponenter(
                    uforetrygdOrdiner = Ytelseskomponent(209, 131),
                    barnetilleggFellesbarn = Ytelseskomponent(7463, 231),
                    barnetilleggSaerkullsbarn = Ytelseskomponent(936, 229),
                    gjenlevendetillegg = Ytelseskomponent(183, 22),
                    ektefelletillegg = Ytelseskomponent(323, 0)
                ), 500000.0, 45000, 530000
            ), false,
            null,
            null,

            null,
            null,
            null,
            false,
            false,
            LocalDate.now(),
            null
        )
        `when`(penClient.simulerInntektsendring(any(), any(), any(), any())).thenReturn(expectedSimuleringsresultat)
        `when`(tokenService.determineLoggedInUser()).thenReturn("Saksbehandler Sak Sakbehandlersen")


        val result = simuleringService.simulerInntektsendring(
            PID, forventedeInntekterOppgitt, forventedeInntekterRegistrert, simuleringsaar, simuleringFomDato
        )

        //Ytelseskomponent for ordinær uføretrygd
        assertEquals(
            expectedSimuleringsresultat.currentUforetrygdSummary.uforetrygdYtelseskomponenter.uforetrygdOrdiner.amountPerYear,
            result.simuleringsresultat.uforetrygd.yearly.before
        )
        assertEquals(
            expectedSimuleringsresultat.currentUforetrygdSummary.uforetrygdYtelseskomponenter.uforetrygdOrdiner.amountPerMonth,
            result.simuleringsresultat.uforetrygd.monthly.before
        )
        assertEquals(
            expectedSimuleringsresultat.simulertUforetrygdSummary.uforetrygdYtelseskomponenter.uforetrygdOrdiner.amountPerYear,
            result.simuleringsresultat.uforetrygd.yearly.after
        )
        assertEquals(
            expectedSimuleringsresultat.simulertUforetrygdSummary.uforetrygdYtelseskomponenter.uforetrygdOrdiner.amountPerMonth,
            result.simuleringsresultat.uforetrygd.monthly.after
        )

        //Ytelseskomponent for barnetillegg fellesbarn
        assertEquals(
            expectedSimuleringsresultat.currentUforetrygdSummary.uforetrygdYtelseskomponenter.barnetilleggFellesbarn?.amountPerYear,
            result.simuleringsresultat.barnetilleggFellesbarn?.yearly?.before
        )
        assertEquals(
            expectedSimuleringsresultat.currentUforetrygdSummary.uforetrygdYtelseskomponenter.barnetilleggFellesbarn?.amountPerMonth,
            result.simuleringsresultat.barnetilleggFellesbarn?.monthly?.before
        )
        assertEquals(
            expectedSimuleringsresultat.simulertUforetrygdSummary.uforetrygdYtelseskomponenter.barnetilleggFellesbarn?.amountPerYear,
            result.simuleringsresultat.barnetilleggFellesbarn?.yearly?.after
        )
        assertEquals(
            expectedSimuleringsresultat.simulertUforetrygdSummary.uforetrygdYtelseskomponenter.barnetilleggFellesbarn?.amountPerMonth,
            result.simuleringsresultat.barnetilleggFellesbarn?.monthly?.after
        )

        //Ytelseskomponent for barnetillegg sørkullsbarn
        assertEquals(
            expectedSimuleringsresultat.currentUforetrygdSummary.uforetrygdYtelseskomponenter.barnetilleggSaerkullsbarn?.amountPerYear,
            result.simuleringsresultat.barnetilleggSaerkullsbarn?.yearly?.before
        )
        assertEquals(
            expectedSimuleringsresultat.currentUforetrygdSummary.uforetrygdYtelseskomponenter.barnetilleggSaerkullsbarn?.amountPerMonth,
            result.simuleringsresultat.barnetilleggSaerkullsbarn?.monthly?.before
        )
        assertEquals(
            expectedSimuleringsresultat.simulertUforetrygdSummary.uforetrygdYtelseskomponenter.barnetilleggSaerkullsbarn?.amountPerYear,
            result.simuleringsresultat.barnetilleggSaerkullsbarn?.yearly?.after
        )
        assertEquals(
            expectedSimuleringsresultat.simulertUforetrygdSummary.uforetrygdYtelseskomponenter.barnetilleggSaerkullsbarn?.amountPerMonth,
            result.simuleringsresultat.barnetilleggSaerkullsbarn?.monthly?.after
        )

        //Ytelseskomponent for gjenlevendetillegg
        assertEquals(
            expectedSimuleringsresultat.currentUforetrygdSummary.uforetrygdYtelseskomponenter.gjenlevendetillegg?.amountPerYear,
            result.simuleringsresultat.gjenlevendetillegg?.yearly?.before
        )
        assertEquals(
            expectedSimuleringsresultat.currentUforetrygdSummary.uforetrygdYtelseskomponenter.gjenlevendetillegg?.amountPerMonth,
            result.simuleringsresultat.gjenlevendetillegg?.monthly?.before
        )
        assertEquals(
            expectedSimuleringsresultat.simulertUforetrygdSummary.uforetrygdYtelseskomponenter.gjenlevendetillegg?.amountPerYear,
            result.simuleringsresultat.gjenlevendetillegg?.yearly?.after
        )
        assertEquals(
            expectedSimuleringsresultat.simulertUforetrygdSummary.uforetrygdYtelseskomponenter.gjenlevendetillegg?.amountPerMonth,
            result.simuleringsresultat.gjenlevendetillegg?.monthly?.after
        )

        //Forventet inntekt
        assertEquals(
            forventedeInntekterRegistrert.sumBenyttedeInntekterBruker,
            result.simuleringsresultat.forventetInntekt.yearly.before
        )
        assertEquals(
            forventedeInntekterRegistrert.sumBenyttedeInntekterBruker / 12,
            result.simuleringsresultat.forventetInntekt.monthly.before
        )
        assertEquals(forventedeInntekterOppgitt.bruker.sum(), result.simuleringsresultat.forventetInntekt.yearly.after)
        assertEquals(
            forventedeInntekterOppgitt.bruker.sum() / 12,
            result.simuleringsresultat.forventetInntekt.monthly.after
        )

    }

    @Test
    fun `should set sum yearly uforetrygd before to sumYtelseskomponenter when simuleringsaar same year as gjeldendeBeregningFom`() {
        val simuleringsaar = LocalDate.now().year
        val simuleringFomDato = LocalDate.now().plusMonths(1).withDayOfMonth(1)
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

        val forventedeInntekterRegistrert = ForventedeInntekterSummary(
            mostRecentForventedeInntekterRegistrertAndBenyttet = no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.ForventedeInntekter(
                bruker = no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.PersonInntekter(
                    null, null, null, null, null
                ), null
            ), sumBenyttedeInntekterBruker = 400000, sumBenyttedeInntekterEps = null
        )
        val expectedSimuleringsresultat = SimulerEndringUforetrygdResponse(
            UforetrygdSummary(
                uforetrygdYtelseskomponenter = UforetrygdYtelseskomponenter(
                    uforetrygdOrdiner = Ytelseskomponent(33, 56),
                    barnetilleggFellesbarn = Ytelseskomponent(30, 275),
                    barnetilleggSaerkullsbarn = Ytelseskomponent(434, 1094),
                    gjenlevendetillegg = Ytelseskomponent(382, 43),
                    ektefelletillegg = Ytelseskomponent(2234, 5039)
                ), 400000.0, 35000, 430000
            ), UforetrygdSummary(
                uforetrygdYtelseskomponenter = UforetrygdYtelseskomponenter(
                    uforetrygdOrdiner = Ytelseskomponent(209, 131),
                    barnetilleggFellesbarn = Ytelseskomponent(7463, 231),
                    barnetilleggSaerkullsbarn = Ytelseskomponent(936, 229),
                    gjenlevendetillegg = Ytelseskomponent(183, 22),
                    ektefelletillegg = Ytelseskomponent(323, 0)
                ), 500000.0, 45000, 530000
            ), false,
            null,
            null,

            null,
            null,
            LocalDate.now(),
            false,
            false,
            LocalDate.now(),
            null
        )
        `when`(penClient.simulerInntektsendring(any(), any(), any(), any())).thenReturn(expectedSimuleringsresultat)
        `when`(tokenService.determineLoggedInUser()).thenReturn("Saksbehandler Sak Sakbehandlersen")


        val result = simuleringService.simulerInntektsendring(
            PID, forventedeInntekterOppgitt, forventedeInntekterRegistrert, simuleringsaar, simuleringFomDato
        )
        assertEquals(expectedSimuleringsresultat.currentUforetrygdSummary.sumYtelseskomponenter, result.simuleringsresultat.sum.yearly.before)
    }

    @Test
    fun `should set sum yearly uforetrygd before to null when simuleringsaar not same year as gjeldendeBeregningFom`() {
        val simuleringsaar = LocalDate.now().year + 1
        val simuleringFomDato = LocalDate.now().plusMonths(1).withDayOfMonth(1)
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

        val forventedeInntekterRegistrert = ForventedeInntekterSummary(
            mostRecentForventedeInntekterRegistrertAndBenyttet = no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.ForventedeInntekter(
                bruker = no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.PersonInntekter(
                    null, null, null, null, null
                ), null
            ), sumBenyttedeInntekterBruker = 400000, sumBenyttedeInntekterEps = null
        )
        val expectedSimuleringsresultat = SimulerEndringUforetrygdResponse(
            UforetrygdSummary(
                uforetrygdYtelseskomponenter = UforetrygdYtelseskomponenter(
                    uforetrygdOrdiner = Ytelseskomponent(33, 56),
                    barnetilleggFellesbarn = Ytelseskomponent(30, 275),
                    barnetilleggSaerkullsbarn = Ytelseskomponent(434, 1094),
                    gjenlevendetillegg = Ytelseskomponent(382, 43),
                    ektefelletillegg = Ytelseskomponent(2234, 5039)
                ), 400000.0, 35000, 430000
            ), UforetrygdSummary(
                uforetrygdYtelseskomponenter = UforetrygdYtelseskomponenter(
                    uforetrygdOrdiner = Ytelseskomponent(209, 131),
                    barnetilleggFellesbarn = Ytelseskomponent(7463, 231),
                    barnetilleggSaerkullsbarn = Ytelseskomponent(936, 229),
                    gjenlevendetillegg = Ytelseskomponent(183, 22),
                    ektefelletillegg = Ytelseskomponent(323, 0)
                ), 500000.0, 45000, 530000
            ), false,
            null,
            null,

            null,
            null,
            LocalDate.now(),
            false,
            false,
            LocalDate.now(),
            null
        )
        `when`(penClient.simulerInntektsendring(any(), any(), any(), any())).thenReturn(expectedSimuleringsresultat)
        `when`(tokenService.determineLoggedInUser()).thenReturn("Saksbehandler Sak Sakbehandlersen")


        val result = simuleringService.simulerInntektsendring(
            PID, forventedeInntekterOppgitt, forventedeInntekterRegistrert, simuleringsaar, simuleringFomDato
        )
        assertNull(result.simuleringsresultat.sum.yearly.before)
    }

    @Test
    fun `should set sum yearly uforetrygd after to sumYtelseskomponenter`() {
        val simuleringsaar = LocalDate.now().year + 1
        val simuleringFomDato = LocalDate.now().plusMonths(1).withDayOfMonth(1)
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

        val forventedeInntekterRegistrert = ForventedeInntekterSummary(
            mostRecentForventedeInntekterRegistrertAndBenyttet = no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.ForventedeInntekter(
                bruker = no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.PersonInntekter(
                    null, null, null, null, null
                ), null
            ), sumBenyttedeInntekterBruker = 400000, sumBenyttedeInntekterEps = null
        )
        val expectedSimuleringsresultat = SimulerEndringUforetrygdResponse(
            UforetrygdSummary(
                uforetrygdYtelseskomponenter = UforetrygdYtelseskomponenter(
                    uforetrygdOrdiner = Ytelseskomponent(33, 56),
                    barnetilleggFellesbarn = Ytelseskomponent(30, 275),
                    barnetilleggSaerkullsbarn = Ytelseskomponent(434, 1094),
                    gjenlevendetillegg = Ytelseskomponent(382, 43),
                    ektefelletillegg = Ytelseskomponent(2234, 5039)
                ), 400000.0, 35000, 430000
            ), UforetrygdSummary(
                uforetrygdYtelseskomponenter = UforetrygdYtelseskomponenter(
                    uforetrygdOrdiner = Ytelseskomponent(209, 131),
                    barnetilleggFellesbarn = Ytelseskomponent(7463, 231),
                    barnetilleggSaerkullsbarn = Ytelseskomponent(936, 229),
                    gjenlevendetillegg = Ytelseskomponent(183, 22),
                    ektefelletillegg = Ytelseskomponent(323, 0)
                ), 500000.0, 45000, 530000
            ), false,
            null,
            null,

            null,
            null,
            LocalDate.now(),
            false,
            false,
            LocalDate.now(),
            null
        )
        `when`(penClient.simulerInntektsendring(any(), any(), any(), any())).thenReturn(expectedSimuleringsresultat)
        `when`(tokenService.determineLoggedInUser()).thenReturn("Saksbehandler Sak Sakbehandlersen")


        val result = simuleringService.simulerInntektsendring(
            PID, forventedeInntekterOppgitt, forventedeInntekterRegistrert, simuleringsaar, simuleringFomDato
        )
        assertEquals(expectedSimuleringsresultat.simulertUforetrygdSummary.sumYtelseskomponenter, result.simuleringsresultat.sum.yearly.after)
    }

    @Test
    fun `should set monthly sums to totalbelopNetto for currentUforetrygd and simulert uforetrygd`(){
        val simuleringsaar = LocalDate.now().year
        val simuleringFomDato = LocalDate.now().plusMonths(1).withDayOfMonth(1)
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

        val forventedeInntekterRegistrert = ForventedeInntekterSummary(
            mostRecentForventedeInntekterRegistrertAndBenyttet = no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.ForventedeInntekter(
                bruker = no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.PersonInntekter(
                    null, null, null, null, null
                ), null
            ), sumBenyttedeInntekterBruker = 400000, sumBenyttedeInntekterEps = null
        )
        val expectedSimuleringsresultat = SimulerEndringUforetrygdResponse(
            UforetrygdSummary(
                uforetrygdYtelseskomponenter = UforetrygdYtelseskomponenter(
                    uforetrygdOrdiner = Ytelseskomponent(33, 56),
                    barnetilleggFellesbarn = Ytelseskomponent(30, 275),
                    barnetilleggSaerkullsbarn = Ytelseskomponent(434, 1094),
                    gjenlevendetillegg = Ytelseskomponent(382, 43),
                    ektefelletillegg = Ytelseskomponent(2234, 5039)
                ), 400000.0, 35000, 430000
            ), UforetrygdSummary(
                uforetrygdYtelseskomponenter = UforetrygdYtelseskomponenter(
                    uforetrygdOrdiner = Ytelseskomponent(209, 131),
                    barnetilleggFellesbarn = Ytelseskomponent(7463, 231),
                    barnetilleggSaerkullsbarn = Ytelseskomponent(936, 229),
                    gjenlevendetillegg = Ytelseskomponent(183, 22),
                    ektefelletillegg = Ytelseskomponent(323, 0)
                ), 500000.0, 45000, 530000
            ), false,
            null,
            null,

            null,
            null,
            LocalDate.now(),
            false,
            false,
            LocalDate.now(),
            null
        )
        `when`(penClient.simulerInntektsendring(any(), any(), any(), any())).thenReturn(expectedSimuleringsresultat)
        `when`(tokenService.determineLoggedInUser()).thenReturn("Saksbehandler Sak Sakbehandlersen")


        val result = simuleringService.simulerInntektsendring(
            PID, forventedeInntekterOppgitt, forventedeInntekterRegistrert, simuleringsaar, simuleringFomDato
        )
        assertEquals(expectedSimuleringsresultat.currentUforetrygdSummary.totalbelopNetto, result.simuleringsresultat.sum.monthly.before)
        assertEquals(expectedSimuleringsresultat.simulertUforetrygdSummary.totalbelopNetto, result.simuleringsresultat.sum.monthly.after)
    }

    private fun assertInntektsgrunnlagEqual(expected: Inntektsgrunnlag, actual: Inntektsgrunnlag) {
        assertEquals(expected.inntektsgrunnlagId, actual.inntektsgrunnlagId)
        assertTrue(expected.fomDato!!.toLocalDate().isEqual(actual.fomDato!!.toLocalDate()))
        assertTrue(expected.tomDato!!.toLocalDate().isEqual(actual.tomDato!!.toLocalDate()))
        assertTrue(expected.endringstidspunkt!!.toLocalDate().isEqual(actual.endringstidspunkt!!.toLocalDate()))
        assertEquals(expected.belop, actual.belop)
        assertEquals(expected.bruk, actual.bruk)
        assertEquals(expected.kopiertFraGammeltKrav, actual.kopiertFraGammeltKrav)
        assertEquals(expected.registerOpprettetAv, actual.registerOpprettetAv)
        assertEquals(expected.grunnlagKilde, actual.grunnlagKilde)
        assertEquals(expected.registerKilde, actual.registerKilde)
        assertEquals(expected.inntektType, actual.inntektType)
        assertEquals(expected.inntektHendelseType, actual.inntektHendelseType)
        assertEquals(expected.grunnIkkeReduksjonType, actual.grunnIkkeReduksjonType)
        assertEquals(expected.persongrunnlagId, actual.persongrunnlagId)
        assertEquals(expected.changeStamp, actual.changeStamp)
        assertEquals(expected.version, actual.version)
    }

    private fun inntektsgrunnlag(simuleringsaar: Int, type: String, belop: Int, opprettetAv: String) = Inntektsgrunnlag(
        inntektsgrunnlagId = null,
        fomDato = OffsetDateTime.ofInstant(
            ZonedDateTime.of(
                simuleringsaar, Month.JANUARY.value, 1, 0, 0, 0, 0, ZoneId.of("Europe/Oslo")
            ).toInstant(), ZoneId.of("Europe/Oslo")
        ),
        tomDato = OffsetDateTime.ofInstant(
            ZonedDateTime.of(
                simuleringsaar, Month.DECEMBER.value, 31, 23, 59, 59, 0, ZoneId.of("Europe/Oslo")
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

    private fun <T> any(): T = Mockito.any()
    fun <T> capture(argumentCaptor: ArgumentCaptor<T>): T = argumentCaptor.capture()

    companion object {
        private const val PID = "00000000001"
    }
}