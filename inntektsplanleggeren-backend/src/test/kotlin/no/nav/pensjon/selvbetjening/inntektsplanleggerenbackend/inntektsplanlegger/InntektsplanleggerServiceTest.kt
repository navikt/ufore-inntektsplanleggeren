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
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.Pensjonsdata
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security.TokenService
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.util.NowProvider
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
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
    fun `should return InntektsplanleggerInitialData from pensjonsdata, validation result and forventede inntekter when constructInitialInntektsplanleggerResponse`() {
        val year = LocalDate.now().year

        val expectedForventetInntektBruker = mapOf(year to 5000)
        val expectedForventetInntektEps = mapOf(year to 6000)
        val expectedInntektsgrense = 232
        val expectedKompensasjonsgrad = 23.2
        val expectedGrenseStoppAvUfoeretrygd = 564654
        val expectedGrenseStoppAvBarnetilleggFellesbarn = 89984
        val expectedGrenseStoppAvBarnetilleggSaerkullsbarn = 29984
        val expectedFribelopFellesbarn = 233423
        val expectedFribelopSaerkullsbarn = 535628

        val pensjonsdata = pensjonsdata(
            inntektsgrense = expectedInntektsgrense,
            kompensasjonsgrad = expectedKompensasjonsgrad,
            grenseStoppAvUfoeretrygd = expectedGrenseStoppAvUfoeretrygd,
            hasGjenlevendeTillegg = true,
            barnetilleggFellesbarn = true,
            barnetilleggSaerkullsbarn = false,
            grenseStoppAvBarnetilleggFellesbarn = expectedGrenseStoppAvBarnetilleggFellesbarn,
            grenseStoppAvBarnetilleggSaerkullsbarn = expectedGrenseStoppAvBarnetilleggSaerkullsbarn,
            fribelopBarnetilleggFellesbarn = expectedFribelopFellesbarn,
            fribelopBarnetilleggSaerkullsbarn = expectedFribelopSaerkullsbarn,
            hasVarigTilrettelagtArbeid = true,
            hasLopendeUforeVedtakThisYear = true
        )
        `when`(penClient.fetchInntektsplanleggerData(PID, LocalDate.now().plusMonths(1).withDayOfMonth(1))).thenReturn(
            pensjonsdata
        )
        `when`(inntektService.getForventedeInntekter(PID, pensjonsdata, year)).thenReturn(
            ForventedeInntekterSummary(
                ForventedeInntekter(
                    PersonInntekter(null, null, null, null, null),
                    null
                ),
                expectedForventetInntektBruker[year]!!,
                expectedForventetInntektEps[year]!!
            )
        )
        `when`(inntektService.getForventedeInntekter(PID, pensjonsdata, year+1)).thenReturn(
            ForventedeInntekterSummary(
                ForventedeInntekter(
                    PersonInntekter(null, null, null, null, null),
                    null
                ),
                mapOf(year to 0)[year]!!,
                mapOf(year to 0)[year]!!
            )
        )
        `when`(validator.validateUserInitialData(any(), any())).thenReturn(emptyList())

        val initialData = inntektsplanleggerService.constructInitialInntektsplanleggerResponse(PID, year)

        assertEquals(expectedForventetInntektBruker, initialData.data!!.forventetInntekt[year]?.let { mapOf(year to it) })
        assertEquals(expectedForventetInntektEps, initialData.data!!.forventetInntektAnnenForelder[year]?.let { mapOf(year to it) })
        assertEquals(expectedInntektsgrense, initialData.data!!.inntektsgrense)
        assertEquals(expectedKompensasjonsgrad, initialData.data!!.kompensasjonsgrad)
        assertEquals(expectedGrenseStoppAvUfoeretrygd, initialData.data!!.grenseStoppAvUfoeretrygd)
        assertTrue(initialData.data!!.hasGjenlevendeTillegg)
        assertTrue(initialData.data!!.hasVarigTilrettelagtArbeid)
        assertFalse(initialData.data!!.hasBarnetilleggSaerkullsbarn)
        assertTrue(initialData.data!!.hasBarneTilleggFellesbarn)
    }

    @Test
    fun `should not set InntektsplanleggerInitialData when error in validation`(){
        val year = LocalDate.now().year
        val expectedMessages = listOf(InntektsplanleggerMessage(InntektsplanleggerMessageCode.FIELD_CAN_NOT_BE_NULL))

        val pensjonsdata = pensjonsdata()
        `when`(penClient.fetchInntektsplanleggerData(PID, LocalDate.now().plusMonths(1).withDayOfMonth(1))).thenReturn(
            pensjonsdata
        )
        `when`(validator.validateUserInitialData(any(), any())).thenReturn(expectedMessages)

        val initialData = inntektsplanleggerService.constructInitialInntektsplanleggerResponse(PID, year)

        assertEquals(expectedMessages, initialData.messages)
        assertNull(initialData.data)

    }

    @Test
    fun `should set annetRelevantAar to previous year when december`() {
        val year = LocalDate.now().year

        val pensjonsdata = pensjonsdata(
            hasLopendeUforeVedtakThisYear = true,
            hasLopendeUforeVedtakNextYear = true
        )
        `when`(penClient.fetchInntektsplanleggerData(PID, LocalDate.now().plusMonths(1).withDayOfMonth(1))).thenReturn(
            pensjonsdata
        )
        `when`(
            inntektService.getForventedeInntekter(
                PID,
                pensjonsdata,
                year
            )
        ).thenReturn(forventedeInntekterRegistrert())
        `when`(inntektService.getForventedeInntekter(PID, pensjonsdata, year + 1)).thenReturn(
            forventedeInntekterRegistrert()
        )
        `when`(validator.validateUserInitialData(any(), any())).thenReturn(emptyList())
        `when`(nowProvider.now()).thenReturn(LocalDate.now().withMonth(Month.DECEMBER.value))

        val initialData = inntektsplanleggerService.constructInitialInntektsplanleggerResponse(PID, year)
        assertEquals(year, initialData.data!!.annetRelevantAar)
    }

    @Test
    fun `should set annetRelevantAar to null when not december`() {
        val year = LocalDate.now().year

        val pensjonsdata = pensjonsdata(
            hasLopendeUforeVedtakThisYear = true,
            hasLopendeUforeVedtakNextYear = true
        )
        `when`(penClient.fetchInntektsplanleggerData(PID, LocalDate.now().plusMonths(1).withDayOfMonth(1))).thenReturn(
            pensjonsdata
        )
        `when`(
            inntektService.getForventedeInntekter(
                PID,
                pensjonsdata,
                year
            )
        ).thenReturn(forventedeInntekterRegistrert())
        `when`(inntektService.getForventedeInntekter(PID, pensjonsdata, year + 1)).thenReturn(
            forventedeInntekterRegistrert()
        )
        `when`(validator.validateUserInitialData(any(), any())).thenReturn(emptyList())
        `when`(nowProvider.now()).thenReturn(LocalDate.now().withMonth(Month.NOVEMBER.value))

        val initialData = inntektsplanleggerService.constructInitialInntektsplanleggerResponse(PID, year)
        assertNull(initialData.data!!.annetRelevantAar)
    }

    @Test
    fun `should set aktuelleAar to current year only when before october and hasLopendeUforeVedtakThisYear`() {
        val year = LocalDate.now().year
        val expectedInntekter = forventedeInntekterRegistrert()

        val pensjonsdata = pensjonsdata(
            hasLopendeUforeVedtakThisYear = true,
            hasLopendeUforeVedtakNextYear = false
        )
        `when`(penClient.fetchInntektsplanleggerData(PID, LocalDate.now().plusMonths(1).withDayOfMonth(1))).thenReturn(
            pensjonsdata
        )
        `when`(inntektService.getForventedeInntekter(PID, pensjonsdata, year)).thenReturn(expectedInntekter)
        `when`(validator.validateUserInitialData(any(), any())).thenReturn(emptyList())
        `when`(nowProvider.now()).thenReturn(LocalDate.now().withMonth(Month.SEPTEMBER.value))

        val initialData = inntektsplanleggerService.constructInitialInntektsplanleggerResponse(PID, year)
        assertEquals(1, initialData.data!!.aktuelleAar.size)
        assertEquals(year, initialData.data!!.aktuelleAar[0])
        assertEquals(setOf(year), initialData.data!!.forventetInntekt.keys)
        assertEquals(expectedInntekter.sumBenyttedeInntekterBruker, initialData.data!!.forventetInntekt[year])
        assertEquals(setOf(year), initialData.data!!.forventetInntektAnnenForelder.keys)
        assertEquals(expectedInntekter.sumBenyttedeInntekterEps, initialData.data!!.forventetInntektAnnenForelder[year])
    }

    @Test
    fun `should set aktuelleAar to current year and next year when after october and hasLopendeUforeVedtakThisYear`() {
        val year = LocalDate.now().year
        val expectedInntekter = forventedeInntekterRegistrert()

        val pensjonsdata = pensjonsdata(
            hasLopendeUforeVedtakThisYear = true,
            hasLopendeUforeVedtakNextYear = false
        )
        `when`(penClient.fetchInntektsplanleggerData(PID, LocalDate.now().plusMonths(1).withDayOfMonth(1))).thenReturn(
            pensjonsdata
        )
        `when`(inntektService.getForventedeInntekter(PID, pensjonsdata, year)).thenReturn(expectedInntekter)
        `when`(inntektService.getForventedeInntekter(PID, pensjonsdata, year + 1)).thenReturn(expectedInntekter)
        `when`(validator.validateUserInitialData(any(), any())).thenReturn(emptyList())
        `when`(nowProvider.now()).thenReturn(LocalDate.now().withMonth(Month.OCTOBER.value))

        val initialData = inntektsplanleggerService.constructInitialInntektsplanleggerResponse(PID, year)
        assertEquals(2, initialData.data!!.aktuelleAar.size)
        assertEquals(year, initialData.data!!.aktuelleAar[0])
        assertEquals(year + 1, initialData.data!!.aktuelleAar[1])

        assertEquals(setOf(year, year + 1), initialData.data!!.forventetInntekt.keys)
        assertEquals(expectedInntekter.sumBenyttedeInntekterBruker, initialData.data!!.forventetInntekt[year])
        assertEquals(setOf(year, year + 1), initialData.data!!.forventetInntektAnnenForelder.keys)
        assertEquals(expectedInntekter.sumBenyttedeInntekterEps, initialData.data!!.forventetInntektAnnenForelder[year])

        assertEquals(expectedInntekter.sumBenyttedeInntekterBruker, initialData.data!!.forventetInntekt[year + 1])
        assertEquals(expectedInntekter.sumBenyttedeInntekterEps, initialData.data!!.forventetInntektAnnenForelder[year + 1])
    }

    @Test
    fun `should set aktuelleAar to next year when after october and hasLopendeUforeVedtakNextYear`() {
        val year = LocalDate.now().year
        val expectedAktueltAar = year + 1
        val expectedInntekter = forventedeInntekterRegistrert()

        val pensjonsdata = pensjonsdata(
            hasLopendeUforeVedtakThisYear = false,
            hasLopendeUforeVedtakNextYear = true
        )
        `when`(penClient.fetchInntektsplanleggerData(PID, LocalDate.now().plusMonths(1).withDayOfMonth(1))).thenReturn(
            pensjonsdata
        )
        `when`(inntektService.getForventedeInntekter(PID, pensjonsdata, expectedAktueltAar)).thenReturn(expectedInntekter)
        `when`(validator.validateUserInitialData(any(), any())).thenReturn(emptyList())
        `when`(nowProvider.now()).thenReturn(LocalDate.now().withMonth(Month.OCTOBER.value))

        val initialData = inntektsplanleggerService.constructInitialInntektsplanleggerResponse(PID, year)
        assertEquals(1, initialData.data!!.aktuelleAar.size)
        assertEquals(expectedAktueltAar, initialData.data!!.aktuelleAar[0])
        assertEquals(setOf(expectedAktueltAar), initialData.data!!.forventetInntekt.keys)
        assertEquals(expectedInntekter.sumBenyttedeInntekterBruker, initialData.data!!.forventetInntekt[expectedAktueltAar])
        assertEquals(setOf(expectedAktueltAar), initialData.data!!.forventetInntektAnnenForelder.keys)
        assertEquals(expectedInntekter.sumBenyttedeInntekterEps, initialData.data!!.forventetInntektAnnenForelder[expectedAktueltAar])
    }

    @Test
    fun `should always set aktuelleAar to next year when december but include inntekter for this year and next year`() {
        val year = LocalDate.now().year
        val expectedInntekter = forventedeInntekterRegistrert()

        val pensjonsdata = pensjonsdata(
            hasLopendeUforeVedtakThisYear = true,
            hasLopendeUforeVedtakNextYear = true
        )
        `when`(penClient.fetchInntektsplanleggerData(PID, LocalDate.now().plusMonths(1).withDayOfMonth(1))).thenReturn(
            pensjonsdata
        )
        `when`(inntektService.getForventedeInntekter(PID, pensjonsdata, year)).thenReturn(expectedInntekter)
        `when`(inntektService.getForventedeInntekter(PID, pensjonsdata, year + 1)).thenReturn(expectedInntekter)
        `when`(validator.validateUserInitialData(any(), any())).thenReturn(emptyList())
        `when`(nowProvider.now()).thenReturn(LocalDate.now().withMonth(Month.DECEMBER.value))

        val initialData = inntektsplanleggerService.constructInitialInntektsplanleggerResponse(PID, year)
        assertEquals(1, initialData.data!!.aktuelleAar.size)
        assertEquals(year + 1, initialData.data!!.aktuelleAar[0])

        assertEquals(setOf(year, year + 1), initialData.data!!.forventetInntekt.keys)
        assertEquals(expectedInntekter.sumBenyttedeInntekterBruker, initialData.data!!.forventetInntekt[year])
        assertEquals(expectedInntekter.sumBenyttedeInntekterBruker, initialData.data!!.forventetInntekt[year + 1])

        assertEquals(setOf(year, year + 1), initialData.data!!.forventetInntektAnnenForelder.keys)
        assertEquals(expectedInntekter.sumBenyttedeInntekterEps, initialData.data!!.forventetInntektAnnenForelder[year])
        assertEquals(expectedInntekter.sumBenyttedeInntekterEps, initialData.data!!.forventetInntektAnnenForelder[year + 1])
    }

    @Test
    fun `should set data to null when validation returns error`() {
        val year = LocalDate.now().year
        val expectedMessages = listOf(InntektsplanleggerMessage(InntektsplanleggerMessageCode.USER_HAS_NO_UFORE))

        val pensjonsdata = pensjonsdata()
        `when`(penClient.fetchInntektsplanleggerData(PID, LocalDate.now().plusMonths(1).withDayOfMonth(1))).thenReturn(
            pensjonsdata
        )
        `when`(validator.validateUserInitialData(any(), any())).thenReturn(expectedMessages)

        val initialData = inntektsplanleggerService.constructInitialInntektsplanleggerResponse(PID, year)
        assertNull(initialData.data)
        assertEquals(expectedMessages, initialData.messages)
    }

    @Test
    fun `should set data to null when pensjonsdata is null`() {
        val year = LocalDate.now().year

        val pensjonsdata = null
        `when`(penClient.fetchInntektsplanleggerData(PID, LocalDate.now().plusMonths(1).withDayOfMonth(1))).thenReturn(
            pensjonsdata
        )
        `when`(validator.validateUserInitialData(any(), any())).thenReturn(emptyList())

        val initialData = inntektsplanleggerService.constructInitialInntektsplanleggerResponse(PID, year)
        assertNull(initialData.data)
        assertTrue(initialData.messages.isEmpty())
    }

    @Test
    fun `should not set data to null when validation returns info or warning`() {
        val year = LocalDate.now().year
        val expectedMessages = listOf(InntektsplanleggerMessage(
            InntektsplanleggerMessageCode.FORVENTET_INNTEKT_THIS_YEAR_USED_NEXT_YEAR_INFO),
            InntektsplanleggerMessage(InntektsplanleggerMessageCode.EPS_INNTEKT_CHANGED))

        val pensjonsdata = pensjonsdata()
        `when`(penClient.fetchInntektsplanleggerData(PID, LocalDate.now().plusMonths(1).withDayOfMonth(1))).thenReturn(
            pensjonsdata
        )
        `when`(inntektService.getForventedeInntekter(PID, pensjonsdata, year)).thenReturn(forventedeInntekterRegistrert())
        `when`(validator.validateUserInitialData(any(), any())).thenReturn(expectedMessages)
        `when`(nowProvider.now()).thenReturn(LocalDate.now().withMonth(Month.OCTOBER.value))

        val initialData = inntektsplanleggerService.constructInitialInntektsplanleggerResponse(PID, year)
        assertNotNull(initialData.data)
        assertEquals(expectedMessages, initialData.messages)
    }

    @Test
    fun `should return InntekterResponse from inntektskomponent data when constructInntekterResponse`() {
        val year = LocalDate.now().year
        val pensjonsdata = pensjonsdata(uforeHeleAaret = true)

        `when`(penClient.fetchInntektsplanleggerData(PID, LocalDate.now().plusMonths(1).withDayOfMonth(1))).thenReturn(
            pensjonsdata
        )
        `when`(inntektService.getInntekterHittilIAar(PID, pensjonsdata, year)).thenReturn(
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
        `when`(inntektService.getForventedeInntekter(PID, pensjonsdata, year)).thenReturn(forventedeInntekterRegistrert())

        val inntektData = inntektsplanleggerService.constructInntekterResponse(PID, year)

        assertEquals(1, inntektData!!.arbeidsinntektOgYtelserHittilIAar.size)
        assertEquals(42.0, inntektData.arbeidsinntektOgYtelserHittilIAar[0].belop)
        assertEquals(3, inntektData.arbeidsinntektOgYtelserHittilIAar[0].maned)
        assertEquals(
            listOf("Arbeidsgiveren", "Arbeidsgiveren 2"),
            inntektData.arbeidsinntektOgYtelserHittilIAar[0].inntektsgivere
        )

        assertEquals(2, inntektData.pensjonFraAndreHittilIAar!!.size)
        assertEquals(106.0, inntektData.pensjonFraAndreHittilIAar!![0].belop)
        assertEquals(4, inntektData.pensjonFraAndreHittilIAar!![0].maned)
        assertEquals(listOf("Nav", "Arbeidsgiveren"), inntektData.pensjonFraAndreHittilIAar!![0].inntektsgivere)
        assertEquals(5436.0, inntektData.pensjonFraAndreHittilIAar!![1].belop)
        assertEquals(5, inntektData.pensjonFraAndreHittilIAar!![1].maned)
        assertEquals(listOf("Arbeidsgiveren"), inntektData.pensjonFraAndreHittilIAar!![1].inntektsgivere)

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
        val pensjonsdata = pensjonsdata(epsPid = "01130101011")

        `when`(penClient.fetchInntektsplanleggerData(PID, LocalDate.now().plusMonths(1).withDayOfMonth(1))).thenReturn(
            pensjonsdata
        )
        `when`(inntektService.getInntekterHittilIAar(PID, pensjonsdata, year)).thenReturn(
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
        `when`(inntektService.getForventedeInntekter(PID, pensjonsdata, year)).thenReturn(forventedeInntekterRegistrert())

        val inntektData = inntektsplanleggerService.constructInntekterResponse(PID, year)

        assertEquals("011301*****", inntektData?.epsPid)
    }

    @Test
    fun `should return SimuleringResponse with simuleringsresultat when validation is OK`() {
        val year = LocalDate.now().year
        val pensjonsdata = pensjonsdata()
        val expectedOppgitteInntekter = oppgitteInntekter()
        val expectedForventedeInntekter = forventedeInntekterRegistrert()
        val expectedValideringsresultatInntekt =
            listOf(InntektsplanleggerMessage(InntektsplanleggerMessageCode.EPS_INNTEKT_CHANGED))
        val expectedValideringsresultatSimulering =
            listOf(InntektsplanleggerMessage(InntektsplanleggerMessageCode.FAKTOROMREGNET_ELLER_MANUELT_OVERSTYRT))
        val expectedSimuleringsresultat = simuleringsresultat()

        `when`(penClient.fetchInntektsplanleggerData(PID, LocalDate.now().plusMonths(1).withDayOfMonth(1))).thenReturn(
            pensjonsdata
        )
        `when`(inntektService.getForventedeInntekter(PID, pensjonsdata, year)).thenReturn(expectedForventedeInntekter)
        `when`(
            validator.validateUserAndInputBeforeSimulering(
                pensjonsdata,
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
        val pensjonsdata = pensjonsdata()
        val expectedOppgitteInntekter = oppgitteInntekter()
        val expectedForventedeInntekter = forventedeInntekterRegistrert()
        val expectedValideringsresultatInntekt =
            listOf(InntektsplanleggerMessage(InntektsplanleggerMessageCode.USER_HAS_NO_UFORE))

        `when`(penClient.fetchInntektsplanleggerData(PID, LocalDate.now().plusMonths(1).withDayOfMonth(1))).thenReturn(
            pensjonsdata
        )
        `when`(inntektService.getForventedeInntekter(PID, pensjonsdata, year)).thenReturn(expectedForventedeInntekter)
        `when`(
            validator.validateUserAndInputBeforeSimulering(
                pensjonsdata,
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
        val pensjonsdata = pensjonsdata()
        val expectedSimuleringFom = LocalDate.now().plusMonths(1).withDayOfMonth(1)

        val expectedLoggedInUser = "brukeren"

        val expectedValideringsresultat = listOf(
            InntektsplanleggerMessage(InntektsplanleggerMessageCode.EPS_INNTEKT_CHANGED)
        )

        val expectedOppgitteInntekter = oppgitteInntekter()
        val expectedSimuleringsresultat = simuleringsresultat()
        val expectedForventedeInntekter = forventedeInntekterRegistrert()

        `when`(penClient.fetchInntektsplanleggerData(PID, expectedSimuleringFom)).thenReturn(pensjonsdata)
        `when`(tokenService.determineLoggedInUser()).thenReturn(expectedLoggedInUser)
        `when`(penClient.sendInntektsendring(any(), any(), anyInt(), any(), any())).thenReturn(
            InnsendingResponse(
                BehandlingStatus.AUTOMATISK_BEHANDLING.name
            )
        )
        `when`(inntektService.getForventedeInntekter(PID, pensjonsdata, year)).thenReturn(expectedForventedeInntekter)
        `when`(
            validator.validateUserAndInputBeforeSimulering(
                pensjonsdata,
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
        val pensjonsdata = pensjonsdata()
        val expectedSimuleringFom = LocalDate.now().plusMonths(1).withDayOfMonth(1)

        val expectedLoggedInUser = "brukeren"

        val expectedValideringsresultat = listOf(
            InntektsplanleggerMessage(InntektsplanleggerMessageCode.EPS_INNTEKT_CHANGED)
        )

        val expectedOppgitteInntekter = oppgitteInntekter()
        val expectedSimuleringsresultat = simuleringsresultat()
        val expectedForventedeInntekter = forventedeInntekterRegistrert()

        `when`(penClient.fetchInntektsplanleggerData(PID, expectedSimuleringFom)).thenReturn(pensjonsdata)
        `when`(tokenService.determineLoggedInUser()).thenReturn(expectedLoggedInUser)
        `when`(penClient.sendInntektsendring(any(), any(), anyInt(), any(), any())).thenReturn(
            InnsendingResponse(
                BehandlingStatus.AUTOMATISK_BEHANDLING.name
            )
        )
        `when`(inntektService.getForventedeInntekter(PID, pensjonsdata, year)).thenReturn(expectedForventedeInntekter)
        `when`(
            validator.validateUserAndInputBeforeSimulering(
                pensjonsdata,
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
        val pensjonsdata = pensjonsdata()
        val expectedSimuleringFom = LocalDate.now().plusMonths(1).withDayOfMonth(1)

        val expectedLoggedInUser = "brukeren"

        val expectedValideringsresultat = listOf(
            InntektsplanleggerMessage(InntektsplanleggerMessageCode.EPS_INNTEKT_CHANGED)
        )

        val expectedOppgitteInntekter = oppgitteInntekter()
        val expectedSimuleringsresultat = simuleringsresultat()
        val expectedForventedeInntekter = forventedeInntekterRegistrert()

        `when`(penClient.fetchInntektsplanleggerData(PID, expectedSimuleringFom)).thenReturn(pensjonsdata)
        `when`(tokenService.determineLoggedInUser()).thenReturn(expectedLoggedInUser)
        `when`(penClient.sendInntektsendring(any(), any(), anyInt(), any(), any())).thenReturn(
            InnsendingResponse(
                BehandlingStatus.INNTEKT_LAGRET.name
            )
        )
        `when`(inntektService.getForventedeInntekter(PID, pensjonsdata, year)).thenReturn(expectedForventedeInntekter)
        `when`(
            validator.validateUserAndInputBeforeSimulering(
                pensjonsdata,
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
        val pensjonsdata = pensjonsdata()
        val expectedSimuleringFom = LocalDate.now().plusMonths(1).withDayOfMonth(1)

        val expectedLoggedInUser = "brukeren"

        val expectedValideringsresultat = listOf(
            InntektsplanleggerMessage(InntektsplanleggerMessageCode.EPS_INNTEKT_CHANGED)
        )

        val expectedOppgitteInntekter = oppgitteInntekter()
        val expectedSimuleringsresultat = simuleringsresultat()
        val expectedForventedeInntekter = forventedeInntekterRegistrert()

        `when`(penClient.fetchInntektsplanleggerData(PID, expectedSimuleringFom)).thenReturn(pensjonsdata)
        `when`(tokenService.determineLoggedInUser()).thenReturn(expectedLoggedInUser)
        `when`(penClient.sendInntektsendring(any(), any(), anyInt(), any(), any())).thenReturn(
            InnsendingResponse(
                "EN_UKJENT_STATUS"
            )
        )
        `when`(inntektService.getForventedeInntekter(PID, pensjonsdata, year)).thenReturn(expectedForventedeInntekter)
        `when`(
            validator.validateUserAndInputBeforeSimulering(
                pensjonsdata,
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
        val pensjonsdata = pensjonsdata()
        val expectedSimuleringFom = LocalDate.now().plusMonths(1).withDayOfMonth(1)

        val expectedLoggedInUser = "brukeren"

        val expectedValideringsresultat = listOf(
            InntektsplanleggerMessage(InntektsplanleggerMessageCode.MISSING_RELEVANT_EPS_INNTEKT_WHEN_BARNETILLEGG_FELLESBARN)
        )

        val expectedOppgitteInntekter = oppgitteInntekter()
        val expectedSimuleringsresultat = simuleringsresultat()
        val expectedForventedeInntekter = forventedeInntekterRegistrert()

        `when`(penClient.fetchInntektsplanleggerData(PID, expectedSimuleringFom)).thenReturn(pensjonsdata)
        `when`(tokenService.determineLoggedInUser()).thenReturn(expectedLoggedInUser)
        `when`(penClient.sendInntektsendring(any(), any(), anyInt(), any(), any())).thenReturn(
            InnsendingResponse(
                BehandlingStatus.INNTEKT_LAGRET.name
            )
        )
        `when`(inntektService.getForventedeInntekter(PID, pensjonsdata, year)).thenReturn(expectedForventedeInntekter)
        `when`(
            validator.validateUserAndInputBeforeSimulering(
                pensjonsdata,
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
    fun `should set simuleringFomDato to first in next month when simuleringsaar is this year`(){
        val year = LocalDate.now().year
        inntektsplanleggerService.constructInitialInntektsplanleggerResponse(PID, year)
        verify(penClient, times(1)).fetchInntektsplanleggerData(any(), capture(virkCaptor))
        assertEquals(LocalDate.now().plusMonths(1).withDayOfMonth(1), virkCaptor.value)
    }

    @Test
    fun `should set simuleringFomDato to first in next year when simuleringsaar is next year`(){
        val year = LocalDate.now().year + 1
        inntektsplanleggerService.constructInitialInntektsplanleggerResponse(PID, year)
        verify(penClient, times(1)).fetchInntektsplanleggerData(any(), capture(virkCaptor))
        assertEquals(LocalDate.now().plusYears(1).withDayOfYear(1), virkCaptor.value)
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

    private fun pensjonsdata(
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
        grenseStoppAvBarnetilleggFellesbarn: Int = 100000,
        grenseStoppAvBarnetilleggSaerkullsbarn: Int = 200000,
        fribelopBarnetilleggSaerkullsbarn: Int = 150000,
        fribelopBarnetilleggFellesbarn: Int = 150000,
        epsPid: String? = null,
        inntekterFromOpenKravBruker: List<Inntektsgrunnlag>? = null,
        inntekterFromOpenKravEps: List<Inntektsgrunnlag>? = null
    ): Pensjonsdata =
        Pensjonsdata(
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