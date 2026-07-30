package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.InntektService
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.dto.Inntektshendelse
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.*
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.simulering.*
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation.InntektsplanleggerMessage
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation.InntektsplanleggerMessageCode
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation.Validator
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.PenClient
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.BehandlingStatus
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.InnsendingResponse
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.Inntektsgrunnlag
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.Uforetrygd
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security.TokenService
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.util.NowProvider
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.time.LocalDate
import java.time.Month
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InntektsplanleggerServiceTest {

    private val tokenService = mock(TokenService::class.java)
    private val validator = mock(Validator::class.java)
    private val inntektService = mock(InntektService::class.java)
    private val simuleringService = mock(SimuleringService::class.java)
    private val penClient = mock(PenClient::class.java)
    private val nowProvider = mock(NowProvider::class.java)

    private val inntektsplanleggerService =
        InntektsplanleggerService(penClient, validator, inntektService, simuleringService, tokenService, nowProvider)

    @Captor
    private lateinit var pidCaptor: ArgumentCaptor<String>

    @Captor
    private lateinit var virkCaptor: ArgumentCaptor<LocalDate>

    @Captor
    private lateinit var simulertTotalbelopNettoCaptor: ArgumentCaptor<Int>

    @Captor
    private lateinit var inntektsgrunnlagCaptor: ArgumentCaptor<List<Inntektsgrunnlag>>

    @BeforeEach
    fun setup() {
        MockitoAnnotations.openMocks(this)
        `when`(nowProvider.now()).thenReturn(LocalDate.now())
    }


    @Test
    fun `should return InntekterResponse from inntektskomponent data when hentAarligeInntekter`() {
        val year = LocalDate.now().year
        val uforetrygd = uforetrygd(uforeHeleAaret = true)

        `when`(penClient.fetchInntektsplanleggerData(PID, LocalDate.now().plusMonths(1).withDayOfMonth(1))).thenReturn(
            uforetrygd
        )
        `when`(inntektService.getInntekterHittilIAar(PID, uforetrygd, year)).thenReturn(
            InntekterHittilIAar(
                arbeidsinntektOgPensjonsgivendeYtelser = listOf(
                    Maanedsinntekt(3, 20.0, "Arbeidsgiveren"),
                    Maanedsinntekt(3, 22.0, "Arbeidsgiveren 2")
                ),
                pensjonerFraAndreEnnFolketrygden = listOf(
                    Maanedsinntekt(4, 50.0, "Nav"),
                    Maanedsinntekt(4, 56.0, "Arbeidsgiveren"),
                    Maanedsinntekt(5, 5436.0, "Arbeidsgiveren")
                ),
                arbeidsinntektOgPensjonsgivendeYtelserEps = listOf(
                    Maanedsinntekt(6, 5054.0, "Nav"),
                    Maanedsinntekt(4, 56.0, "Arbeidsgiveren"),
                    Maanedsinntekt(5, 5436.0, "Arbeidsgiveren")
                ),
                pensjonerFraAndreEnnFolketrygdenEps = listOf(
                    Maanedsinntekt(1, 50343.0, "Nav"),
                    Maanedsinntekt(4, 5466.0, "Arbeidsgiveren"),
                    Maanedsinntekt(5, 54436.0, "Arbeidsgiveren")
                ),
            )
        )
        `when`(inntektService.getForventedeInntekter(PID, uforetrygd, year)).thenReturn(forventedeInntekterRegistrert())

        val inntektData = inntektsplanleggerService.hentInntekter(PID, year, true)

        assertEquals(1, inntektData!!.arbeidsinntektOgYtelserHittilIAar.size)
        assertEquals(42.0, inntektData.arbeidsinntektOgYtelserHittilIAar[0].belop)
        assertEquals(3, inntektData.arbeidsinntektOgYtelserHittilIAar[0].maned)
        assertEquals(
            listOf("Arbeidsgiveren", "Arbeidsgiveren 2"),
            inntektData.arbeidsinntektOgYtelserHittilIAar[0].inntektsgivere
        )

        assertEquals(2, inntektData.pensjonFraAndreHittilIAar!!.size)
        assertEquals(106.0, inntektData.pensjonFraAndreHittilIAar[0].belop)
        assertEquals(4, inntektData.pensjonFraAndreHittilIAar[0].maned)
        assertEquals(listOf("Nav", "Arbeidsgiveren"), inntektData.pensjonFraAndreHittilIAar[0].inntektsgivere)
        assertEquals(5436.0, inntektData.pensjonFraAndreHittilIAar[1].belop)
        assertEquals(5, inntektData.pensjonFraAndreHittilIAar[1].maned)
        assertEquals(listOf("Arbeidsgiveren"), inntektData.pensjonFraAndreHittilIAar[1].inntektsgivere)

        assertEquals(1, inntektData.forventedeInntekter?.bruker?.arbeidsinntekt)
        assertEquals(2, inntektData.forventedeInntekter?.bruker?.naeringsinntekt)
        assertEquals(3, inntektData.forventedeInntekter?.bruker?.inntektUtland)
        assertEquals(4, inntektData.forventedeInntekter?.bruker?.andrePensjonsgivendeYtelser)
        assertEquals(5, inntektData.forventedeInntekter?.bruker?.pensjonUtland)

        assertEquals(6, inntektData.forventedeInntekter?.eps?.arbeidsinntekt)
        assertEquals(7, inntektData.forventedeInntekter?.eps?.naeringsinntekt)
        assertEquals(8, inntektData.forventedeInntekter?.eps?.inntektUtland)
        assertEquals(9, inntektData.forventedeInntekter?.eps?.andrePensjonsgivendeYtelser)
        assertEquals(10, inntektData.forventedeInntekter?.eps?.pensjonUtland)

        assertTrue(inntektData.uforeHeleAaret)
    }

    @Test
    fun `should include masked epsPid in InntekterResponse when eps present on uforetrygd sak`(){
        val year = LocalDate.now().year
        val uforetrygd = uforetrygd(epsPid = "01130101011")

        `when`(penClient.fetchInntektsplanleggerData(PID, LocalDate.now().plusMonths(1).withDayOfMonth(1))).thenReturn(
            uforetrygd
        )
        `when`(inntektService.getInntekterHittilIAar(PID, uforetrygd, year)).thenReturn(
            InntekterHittilIAar(
                arbeidsinntektOgPensjonsgivendeYtelser = listOf(
                    Maanedsinntekt(3, 20.0, "Arbeidsgiveren")
                ),
                pensjonerFraAndreEnnFolketrygden = listOf(
                    Maanedsinntekt(4, 50.0, "Nav")
                ),
                arbeidsinntektOgPensjonsgivendeYtelserEps = listOf(
                    Maanedsinntekt(6, 5054.0, "Nav")
                ),
                pensjonerFraAndreEnnFolketrygdenEps = listOf(
                    Maanedsinntekt(1, 50343.0, "Nav")
                ),
            )
        )
        `when`(inntektService.getForventedeInntekter(PID, uforetrygd, year)).thenReturn(forventedeInntekterRegistrert())

        val inntektData = inntektsplanleggerService.hentInntekter(PID, year, true)

        assertEquals("011301*****", inntektData?.epsPid)
    }

    @Test
    fun `should return SimuleringResponse with simuleringsresultat when validation is OK`() {
        val year = LocalDate.now().year
        val uforetrygd = uforetrygd()
        val expectedOppgitteInntekter = oppgitteInntekter()
        val expectedForventedeInntekter = forventedeInntekterRegistrert()
        val expectedValideringsresultatInntekt =
            listOf(InntektsplanleggerMessage(InntektsplanleggerMessageCode.EPS_INNTEKT_CHANGED))
        val expectedValideringsresultatSimulering =
            listOf(InntektsplanleggerMessage(InntektsplanleggerMessageCode.FAKTOROMREGNET_ELLER_MANUELT_OVERSTYRT))
        val expectedSimuleringsresultat = simuleringsresultat()

        `when`(penClient.fetchInntektsplanleggerData(PID, LocalDate.now().plusMonths(1).withDayOfMonth(1))).thenReturn(
            uforetrygd
        )
        `when`(inntektService.getForventedeInntekter(PID, uforetrygd, year)).thenReturn(expectedForventedeInntekter)
        `when`(
            validator.validateUserAndInputBeforeSimulering(
                uforetrygd,
                expectedOppgitteInntekter,
                expectedForventedeInntekter,
                PID,
                year,
                emptyList()
            )
        ).thenReturn(
            expectedValideringsresultatInntekt
        )

        `when`(
            simuleringService.simulerInntektsendring(
                PID,
                expectedOppgitteInntekter,
                expectedForventedeInntekter,
                year,
                LocalDate.now().plusMonths(1).withDayOfMonth(1)
            )
        ).thenReturn(
            SimuleringData(expectedValideringsresultatSimulering, expectedSimuleringsresultat)
        )

        val simulering = inntektsplanleggerService.simulerInntektsendring(PID, year, expectedOppgitteInntekter)

        assertEquals(expectedValideringsresultatInntekt + expectedValideringsresultatSimulering, simulering.messages)
        assertEquals(expectedSimuleringsresultat, simulering.result)
    }

    @Test
    fun `should return SimuleringResponse without simuleringsresultat when validation returns ERROR`() {
        val year = LocalDate.now().year
        val uforetrygd = uforetrygd()
        val expectedOppgitteInntekter = oppgitteInntekter()
        val expectedForventedeInntekter = forventedeInntekterRegistrert()
        val expectedValideringsresultatInntekt =
            listOf(InntektsplanleggerMessage(InntektsplanleggerMessageCode.USER_HAS_NO_UFORE))

        `when`(penClient.fetchInntektsplanleggerData(PID, LocalDate.now().plusMonths(1).withDayOfMonth(1))).thenReturn(
            uforetrygd
        )
        `when`(inntektService.getForventedeInntekter(PID, uforetrygd, year)).thenReturn(expectedForventedeInntekter)
        `when`(
            validator.validateUserAndInputBeforeSimulering(
                uforetrygd,
                expectedOppgitteInntekter,
                expectedForventedeInntekter,
                PID,
                year,
                emptyList()
            )
        ).thenReturn(
            expectedValideringsresultatInntekt
        )

        val simulering = inntektsplanleggerService.simulerInntektsendring(PID, year, expectedOppgitteInntekter)

        assertEquals(expectedValideringsresultatInntekt, simulering.messages)
        assertNull(simulering.result)
    }

    @Test
    fun `should construct correct request to PEN when sendInntektsendring`() {
        val year = LocalDate.now().year
        val uforetrygd = uforetrygd()
        val expectedSimuleringFom = LocalDate.now().plusMonths(1).withDayOfMonth(1)

        val expectedLoggedInUser = "brukeren"

        val expectedValideringsresultat = listOf(
            InntektsplanleggerMessage(InntektsplanleggerMessageCode.EPS_INNTEKT_CHANGED)
        )

        val expectedOppgitteInntekter = oppgitteInntekter()
        val expectedSimuleringsresultat = simuleringsresultat()
        val expectedForventedeInntekter = forventedeInntekterRegistrert()

        `when`(penClient.fetchInntektsplanleggerData(PID, expectedSimuleringFom)).thenReturn(uforetrygd)
        `when`(tokenService.determineLoggedInUser()).thenReturn(expectedLoggedInUser)
        `when`(penClient.sendInntektsendring(any(), any(), anyInt(), any(), any())).thenReturn(
            InnsendingResponse(
                BehandlingStatus.AUTOMATISK_BEHANDLING.name
            )
        )
        `when`(inntektService.getForventedeInntekter(PID, uforetrygd, year)).thenReturn(expectedForventedeInntekter)
        `when`(
            validator.validateUserAndInputBeforeSimulering(
                uforetrygd,
                expectedOppgitteInntekter,
                expectedForventedeInntekter,
                PID,
                year,
                listOf(year)
            )
        ).thenReturn(expectedValideringsresultat)
        `when`(
            simuleringService.simulerInntektsendring(
                PID,
                expectedOppgitteInntekter,
                expectedForventedeInntekter,
                year,
                expectedSimuleringFom
            )
        ).thenReturn(
            SimuleringData(emptyList(), expectedSimuleringsresultat)
        )

        inntektsplanleggerService.sendInntektsendring(PID, year, expectedOppgitteInntekter)

        verify(penClient, times(1)).sendInntektsendring(
            capture(pidCaptor),
            capture(virkCaptor),
            capture(simulertTotalbelopNettoCaptor),
            capture(inntektsgrunnlagCaptor),
            capture(inntektsgrunnlagCaptor)
        )

        assertEquals(PID, pidCaptor.value)
        assertEquals(expectedSimuleringFom, virkCaptor.value)
        assertEquals(expectedSimuleringsresultat.sum.monthly.after, simulertTotalbelopNettoCaptor.value)

        val inntektsgrunnlagBruker = inntektsgrunnlagCaptor.allValues[0]
        assertInnteksgrunnlag(
            expectedOppgitteInntekter.bruker.arbeidsinntekt,
            "FORINTARB",
            expectedLoggedInUser,
            inntektsgrunnlagBruker[0]
        )
        assertInnteksgrunnlag(
            expectedOppgitteInntekter.bruker.naeringsinntekt,
            "FORINTNAE",
            expectedLoggedInUser,
            inntektsgrunnlagBruker[1]
        )
        assertInnteksgrunnlag(
            expectedOppgitteInntekter.bruker.inntektUtland,
            "FORINTUTL",
            expectedLoggedInUser,
            inntektsgrunnlagBruker[3]
        )
        assertInnteksgrunnlag(
            expectedOppgitteInntekter.bruker.pensjonUtland,
            "FORPENUTL",
            expectedLoggedInUser,
            inntektsgrunnlagBruker[4]
        )
        assertInnteksgrunnlag(
            expectedOppgitteInntekter.bruker.andrePensjonsgivendeYtelser,
            "FORINTAND",
            expectedLoggedInUser,
            inntektsgrunnlagBruker[2]
        )

        val inntektsgrunnlagEps = inntektsgrunnlagCaptor.allValues[1]
        assertInnteksgrunnlag(
            expectedOppgitteInntekter.eps?.arbeidsinntekt,
            "FORINTARB",
            expectedLoggedInUser,
            inntektsgrunnlagEps[0]
        )
        assertInnteksgrunnlag(
            expectedOppgitteInntekter.eps?.naeringsinntekt,
            "FORINTNAE",
            expectedLoggedInUser,
            inntektsgrunnlagEps[1]
        )
        assertInnteksgrunnlag(
            expectedOppgitteInntekter.eps?.inntektUtland,
            "FORINTUTL",
            expectedLoggedInUser,
            inntektsgrunnlagEps[3]
        )
        assertInnteksgrunnlag(
            expectedOppgitteInntekter.eps?.pensjonUtland,
            "FORPENUTL",
            expectedLoggedInUser,
            inntektsgrunnlagEps[4]
        )
        assertInnteksgrunnlag(
            expectedOppgitteInntekter.eps?.andrePensjonsgivendeYtelser,
            "FORINTAND",
            expectedLoggedInUser,
            inntektsgrunnlagEps[2]
        )
    }

    @Test
    fun `should return status AUTOMATISK_BEHANDLING when AUTOMATISK_BEHANDLING status from innsending in PEN`() {
        val year = LocalDate.now().year
        val uforetrygd = uforetrygd()
        val expectedSimuleringFom = LocalDate.now().plusMonths(1).withDayOfMonth(1)

        val expectedLoggedInUser = "brukeren"

        val expectedValideringsresultat = listOf(
            InntektsplanleggerMessage(InntektsplanleggerMessageCode.EPS_INNTEKT_CHANGED)
        )

        val expectedOppgitteInntekter = oppgitteInntekter()
        val expectedSimuleringsresultat = simuleringsresultat()
        val expectedForventedeInntekter = forventedeInntekterRegistrert()

        `when`(penClient.fetchInntektsplanleggerData(PID, expectedSimuleringFom)).thenReturn(uforetrygd)
        `when`(tokenService.determineLoggedInUser()).thenReturn(expectedLoggedInUser)
        `when`(penClient.sendInntektsendring(any(), any(), anyInt(), any(), any())).thenReturn(
            InnsendingResponse(
                BehandlingStatus.AUTOMATISK_BEHANDLING.name
            )
        )
        `when`(inntektService.getForventedeInntekter(PID, uforetrygd, year)).thenReturn(expectedForventedeInntekter)
        `when`(
            validator.validateUserAndInputBeforeSimulering(
                uforetrygd,
                expectedOppgitteInntekter,
                expectedForventedeInntekter,
                PID,
                year,
                emptyList()
            )
        ).thenReturn(expectedValideringsresultat)
        `when`(
            simuleringService.simulerInntektsendring(
                PID,
                expectedOppgitteInntekter,
                expectedForventedeInntekter,
                year,
                expectedSimuleringFom
            )
        ).thenReturn(
            SimuleringData(emptyList(), expectedSimuleringsresultat)
        )

        val innsending = inntektsplanleggerService.sendInntektsendring(PID, year, expectedOppgitteInntekter)


        assertEquals(InnsendingStatus.AUTOMATISK_BEHANDLING, innsending.status)
        assertEquals(expectedValideringsresultat, innsending.messages)

    }

    @Test
    fun `should return status INNTEKT_LAGRET_INGEN_BEHANDLING when INNTEKT_LAGRET status from innsending in PEN`() {
        val year = LocalDate.now().year
        val uforetrygd = uforetrygd()
        val expectedSimuleringFom = LocalDate.now().plusMonths(1).withDayOfMonth(1)

        val expectedLoggedInUser = "brukeren"

        val expectedValideringsresultat = listOf(
            InntektsplanleggerMessage(InntektsplanleggerMessageCode.EPS_INNTEKT_CHANGED)
        )

        val expectedOppgitteInntekter = oppgitteInntekter()
        val expectedSimuleringsresultat = simuleringsresultat()
        val expectedForventedeInntekter = forventedeInntekterRegistrert()

        `when`(penClient.fetchInntektsplanleggerData(PID, expectedSimuleringFom)).thenReturn(uforetrygd)
        `when`(tokenService.determineLoggedInUser()).thenReturn(expectedLoggedInUser)
        `when`(penClient.sendInntektsendring(any(), any(), anyInt(), any(), any())).thenReturn(
            InnsendingResponse(
                BehandlingStatus.INNTEKT_LAGRET.name
            )
        )
        `when`(inntektService.getForventedeInntekter(PID, uforetrygd, year)).thenReturn(expectedForventedeInntekter)
        `when`(
            validator.validateUserAndInputBeforeSimulering(
                uforetrygd,
                expectedOppgitteInntekter,
                expectedForventedeInntekter,
                PID,
                year,
                emptyList()
            )
        ).thenReturn(expectedValideringsresultat)
        `when`(
            simuleringService.simulerInntektsendring(
                PID,
                expectedOppgitteInntekter,
                expectedForventedeInntekter,
                year,
                expectedSimuleringFom
            )
        ).thenReturn(
            SimuleringData(emptyList(), expectedSimuleringsresultat)
        )

        val innsending = inntektsplanleggerService.sendInntektsendring(PID, year, expectedOppgitteInntekter)


        assertEquals(InnsendingStatus.INNTEKT_LAGRET_INGEN_BEHANDLING, innsending.status)
        assertEquals(expectedValideringsresultat, innsending.messages)
    }

    @Test
    fun `should return status IKKE_SENDT when PEN returns unknown status`() {
        val year = LocalDate.now().year
        val uforetrygd = uforetrygd()
        val expectedSimuleringFom = LocalDate.now().plusMonths(1).withDayOfMonth(1)

        val expectedLoggedInUser = "brukeren"

        val expectedValideringsresultat = listOf(
            InntektsplanleggerMessage(InntektsplanleggerMessageCode.EPS_INNTEKT_CHANGED)
        )

        val expectedOppgitteInntekter = oppgitteInntekter()
        val expectedSimuleringsresultat = simuleringsresultat()
        val expectedForventedeInntekter = forventedeInntekterRegistrert()

        `when`(penClient.fetchInntektsplanleggerData(PID, expectedSimuleringFom)).thenReturn(uforetrygd)
        `when`(tokenService.determineLoggedInUser()).thenReturn(expectedLoggedInUser)
        `when`(penClient.sendInntektsendring(any(), any(), anyInt(), any(), any())).thenReturn(
            InnsendingResponse(
                "EN_UKJENT_STATUS"
            )
        )
        `when`(inntektService.getForventedeInntekter(PID, uforetrygd, year)).thenReturn(expectedForventedeInntekter)
        `when`(
            validator.validateUserAndInputBeforeSimulering(
                uforetrygd,
                expectedOppgitteInntekter,
                expectedForventedeInntekter,
                PID,
                year,
                emptyList()
            )
        ).thenReturn(expectedValideringsresultat)
        `when`(
            simuleringService.simulerInntektsendring(
                PID,
                expectedOppgitteInntekter,
                expectedForventedeInntekter,
                year,
                expectedSimuleringFom
            )
        ).thenReturn(
            SimuleringData(emptyList(), expectedSimuleringsresultat)
        )

        val innsending = inntektsplanleggerService.sendInntektsendring(PID, year, expectedOppgitteInntekter)


        assertEquals(InnsendingStatus.IKKE_SENDT, innsending.status)
        assertEquals(expectedValideringsresultat, innsending.messages)
    }

    @Test
    fun `should return status IKKE_SENDT_VALIDERING_FEILET when validation is failing before innsending to PEN`() {
        val year = LocalDate.now().year
        val uforetrygd = uforetrygd()
        val expectedSimuleringFom = LocalDate.now().plusMonths(1).withDayOfMonth(1)

        val expectedLoggedInUser = "brukeren"

        val expectedValideringsresultat = listOf(
            InntektsplanleggerMessage(InntektsplanleggerMessageCode.MISSING_RELEVANT_EPS_INNTEKT_WHEN_BARNETILLEGG_FELLESBARN)
        )

        val expectedOppgitteInntekter = oppgitteInntekter()
        val expectedSimuleringsresultat = simuleringsresultat()
        val expectedForventedeInntekter = forventedeInntekterRegistrert()

        `when`(penClient.fetchInntektsplanleggerData(PID, expectedSimuleringFom)).thenReturn(uforetrygd)
        `when`(tokenService.determineLoggedInUser()).thenReturn(expectedLoggedInUser)
        `when`(penClient.sendInntektsendring(any(), any(), anyInt(), any(), any())).thenReturn(
            InnsendingResponse(
                BehandlingStatus.INNTEKT_LAGRET.name
            )
        )
        `when`(inntektService.getForventedeInntekter(PID, uforetrygd, year)).thenReturn(expectedForventedeInntekter)
        `when`(
            validator.validateUserAndInputBeforeSimulering(
                uforetrygd,
                expectedOppgitteInntekter,
                expectedForventedeInntekter,
                PID,
                year,
                emptyList()
            )
        ).thenReturn(expectedValideringsresultat)
        `when`(
            simuleringService.simulerInntektsendring(
                PID,
                expectedOppgitteInntekter,
                expectedForventedeInntekter,
                year,
                expectedSimuleringFom
            )
        ).thenReturn(
            SimuleringData(emptyList(), expectedSimuleringsresultat)
        )

        val innsending = inntektsplanleggerService.sendInntektsendring(PID, year, expectedOppgitteInntekter)


        assertEquals(InnsendingStatus.IKKE_SENDT_VALIDERING_FEILET, innsending.status)
        assertEquals(expectedValideringsresultat, innsending.messages)
    }

    @Test
    fun `finnAarKanRegistrereFor skal returnere tom liste når bruker ikke har løpende vedtak`() {
        val år = LocalDate.now().year
        val uforetrygd = uforetrygd(
            hasLopendeUforeVedtakThisYear = false,
            hasLopendeUforeVedtakNextYear = false
        )

        `when`(nowProvider.now()).thenReturn(LocalDate.now().withMonth(Month.JUNE.value))

        val aarKanRegistrereFor = inntektsplanleggerService.finnAarKanRegistrereFor( uforetrygd.hasLopendeUforeVedtakThisYear, uforetrygd.hasLopendeUforeVedtakNextYear)
        assertEquals(emptyList(), aarKanRegistrereFor)
    }

    @Test
    fun `finnAarKanRegistrereFor skal returnere bare dette år når før oktober og bruker har løpende vedtak`() {
        val år = LocalDate.now().year
        val uforetrygd = uforetrygd(
            hasLopendeUforeVedtakThisYear = true,
            hasLopendeUforeVedtakNextYear = true
        )

        `when`(nowProvider.now()).thenReturn(LocalDate.now().withMonth(Month.JUNE.value))

        val aarKanRegistrereFor = inntektsplanleggerService.finnAarKanRegistrereFor( uforetrygd.hasLopendeUforeVedtakThisYear, uforetrygd.hasLopendeUforeVedtakNextYear)
        assertEquals(listOf(år), aarKanRegistrereFor)
    }

    @Test
    fun `finnAarKanRegistrereFor skal returnere dette år og neste år når mellom oktober og desember og bruker har løpende vedtak begge år`() {
        val iÅr = LocalDate.now().year
        val nesteÅr = iÅr +1
        val uforetrygd = uforetrygd(
            hasLopendeUforeVedtakThisYear = true,
            hasLopendeUforeVedtakNextYear = true
        )

        `when`(nowProvider.now()).thenReturn(LocalDate.now().withMonth(Month.OCTOBER.value))

        val aarKanRegistrereFor = inntektsplanleggerService.finnAarKanRegistrereFor( uforetrygd.hasLopendeUforeVedtakThisYear, uforetrygd.hasLopendeUforeVedtakNextYear)
        assertEquals(listOf(iÅr, nesteÅr), aarKanRegistrereFor)
    }

    @Test
    fun `finnAarKanRegistrereFor skal returnere neste år når mellom oktober og desember og bruker har løpende vedtak neste år`() {
        val iÅr = LocalDate.now().year
        val nesteÅr = iÅr + 1
        val uforetrygd = uforetrygd(
            hasLopendeUforeVedtakThisYear = false,
            hasLopendeUforeVedtakNextYear = true
        )

        `when`(nowProvider.now()).thenReturn(LocalDate.now().withMonth(Month.OCTOBER.value))

        val aarKanRegistrereFor = inntektsplanleggerService.finnAarKanRegistrereFor( uforetrygd.hasLopendeUforeVedtakThisYear, uforetrygd.hasLopendeUforeVedtakNextYear)
        assertEquals(listOf(nesteÅr), aarKanRegistrereFor)
    }

    @Test
    fun `finnAarKanRegistrereFor skal returnere neste år når desember og bruker har løpende vedtak`() {
        val iÅr = LocalDate.now().year
        val nesteÅr = iÅr + 1
        val uforetrygd = uforetrygd(
            hasLopendeUforeVedtakThisYear = true,
            hasLopendeUforeVedtakNextYear = true
        )

        `when`(nowProvider.now()).thenReturn(LocalDate.now().withMonth(Month.DECEMBER.value))

        val aarKanRegistrereFor = inntektsplanleggerService.finnAarKanRegistrereFor( uforetrygd.hasLopendeUforeVedtakThisYear, uforetrygd.hasLopendeUforeVedtakNextYear)
        assertEquals(listOf(nesteÅr), aarKanRegistrereFor)
    }

    @Test
    fun `getSimuleringFomDato skal være første neste måned når simuleringsår er i år`() {
        val iÅr = LocalDate.now().year

        val simuleringFomDato = inntektsplanleggerService.getSimuleringFomDato(iÅr)
        assertEquals(LocalDate.now().plusMonths(1).withDayOfMonth(1), simuleringFomDato)
    }

    @Test
    fun `getSimuleringFomDato skal være første neste år når simuleringsår er neste år`() {
        val nesteÅr = LocalDate.now().year + 1

        val simuleringFomDato = inntektsplanleggerService.getSimuleringFomDato(nesteÅr)
        assertEquals(LocalDate.now().plusYears(1).withDayOfMonth(1).withMonth(1), simuleringFomDato)
    }

    private fun assertInnteksgrunnlag(
        expectedBelop: Int?,
        expectedType: String,
        expectedOpprettetAv: String,
        inntektsgrunnlag: Inntektsgrunnlag,
    ) {
        assertEquals(expectedBelop, inntektsgrunnlag.belop)
        assertEquals(expectedType, inntektsgrunnlag.inntektType)
        assertTrue(inntektsgrunnlag.bruk)
        assertNull(inntektsgrunnlag.inntektsgrunnlagId)
        assertFalse(inntektsgrunnlag.kopiertFraGammeltKrav)
        assertEquals(expectedOpprettetAv, inntektsgrunnlag.registerOpprettetAv)
        assertEquals("BRUKER_OPP", inntektsgrunnlag.grunnlagKilde)
        assertEquals("SELVBETJ", inntektsgrunnlag.registerKilde)
        assertEquals("BENYTTET", inntektsgrunnlag.inntektHendelseType)
        assertNull(inntektsgrunnlag.grunnIkkeReduksjonType)
        assertNull(inntektsgrunnlag.persongrunnlagId)
        assertNull(inntektsgrunnlag.changeStamp)
        assertNull(inntektsgrunnlag.version)
    }

    private fun oppgitteInntekter() =
        no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.inntekt.ForventedeInntekter(
            bruker = no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.inntekt.PersonInntekter(
                arbeidsinntekt = 45,
                naeringsinntekt = 3453,
                inntektUtland = 24,
                andrePensjonsgivendeYtelser = 67,
                pensjonUtland = 435
            ),
            eps = no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.inntekt.PersonInntekter(
                arbeidsinntekt = 2425,
                naeringsinntekt = 5646,
                inntektUtland = 6577,
                andrePensjonsgivendeYtelser = 34535,
                pensjonUtland = 65756
            )
        )

    private fun simuleringsresultat() = Simuleringsresultat(
        SimuleringAmounts(
            BeforeAndAfterValues(10233, 8823), BeforeAndAfterValues(100923, 89023)
        ), SimuleringAmounts(
            BeforeAndAfterValues(0, 0), BeforeAndAfterValues(0, 0)
        ), null, null, null, SimuleringAmounts(
            BeforeAndAfterValues(10233, 8823), BeforeAndAfterValues(100923, 89023)
        )
    )

    private fun forventedeInntekterRegistrert() = ForventedeInntekterSummary(
        ForventedeInntekter(
            PersonInntekter(
                arbeidsinntekt = Personinntekt(1, Inntektshendelse.BENYTTET),
                naeringsinntekt = Personinntekt(2, Inntektshendelse.BENYTTET),
                inntektUtland = Personinntekt(3, Inntektshendelse.BENYTTET),
                andrePensjonsgivendeYtelser = Personinntekt(4, Inntektshendelse.BENYTTET),
                pensjonUtland = Personinntekt(5, Inntektshendelse.BENYTTET)
            ),
            PersonInntekter(
                arbeidsinntekt = Personinntekt(6, Inntektshendelse.BENYTTET),
                naeringsinntekt = Personinntekt(7, Inntektshendelse.BENYTTET),
                inntektUtland = Personinntekt(8, Inntektshendelse.BENYTTET),
                andrePensjonsgivendeYtelser = Personinntekt(9, Inntektshendelse.BENYTTET),
                pensjonUtland = Personinntekt(10, Inntektshendelse.BENYTTET)
            )
        ),
        15,
        40
    )

    private fun uforetrygd(
        inntektsgrense: Int = 300000,
        kompensasjonsgrad: Double = 65.5,
        grenseStoppAvUfoeretrygd: Int = 500000,
        hasLopendeUforeVedtakThisYear: Boolean = false,
        hasLopendeUforeVedtakNextYear: Boolean = false,
        hasVarigTilrettelagtArbeid: Boolean = false,
        hasGjenlevendeTillegg: Boolean = false,
        uforeHeleAaret: Boolean = false,
        barnetilleggFellesbarn: Boolean = false,
        barnetilleggSaerkullsbarn: Boolean = false,
        epsPid: String? = null,
        inntekterFromOpenKravBruker: List<Inntektsgrunnlag>? = null,
        inntekterFromOpenKravEps: List<Inntektsgrunnlag>? = null
    ): Uforetrygd =
        Uforetrygd(
            inntektsgrense = inntektsgrense,
            kompensasjonsgrad = kompensasjonsgrad,
            grenseStoppAvUfoeretrygd = grenseStoppAvUfoeretrygd,
            hasLopendeUforeVedtakThisYear = hasLopendeUforeVedtakThisYear,
            hasLopendeUforeVedtakNextYear = hasLopendeUforeVedtakNextYear,
            hasVarigTilrettelagtArbeid = hasVarigTilrettelagtArbeid,
            hasGjenlevendeTillegg = hasGjenlevendeTillegg,
            uforeHeleAaret = uforeHeleAaret,
            barnetilleggSaerkullsbarn = barnetilleggSaerkullsbarn,
            barnetilleggFellesbarn = barnetilleggFellesbarn,
            epsPid = epsPid,
            inntekterFromOpenKravBruker = inntekterFromOpenKravBruker,
            inntekterFromOpenKravEps = inntekterFromOpenKravEps,
            uforeFomDato = null
        )

    private fun <T> any(): T = Mockito.any()
    fun <T> capture(argumentCaptor: ArgumentCaptor<T>): T = argumentCaptor.capture()

    companion object {
        private const val PID = "00000000001"
    }
}