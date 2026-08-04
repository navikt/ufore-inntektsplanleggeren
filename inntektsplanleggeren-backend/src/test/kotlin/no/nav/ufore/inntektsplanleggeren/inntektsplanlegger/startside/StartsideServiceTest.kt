package no.nav.ufore.inntektsplanleggeren.inntektsplanlegger.startside

import no.nav.ufore.inntektsplanleggeren.inntekt.InntektService
import no.nav.ufore.inntektsplanleggeren.inntekt.dto.Inntektshendelse
import no.nav.ufore.inntektsplanleggeren.inntekt.model.ForventedeInntekter
import no.nav.ufore.inntektsplanleggeren.inntekt.model.ForventedeInntekterSummary
import no.nav.ufore.inntektsplanleggeren.inntekt.model.PersonInntekter
import no.nav.ufore.inntektsplanleggeren.inntektsplanlegger.InntektsplanleggerService
import no.nav.ufore.inntektsplanleggeren.inntektsplanlegger.validation.InntektsplanleggerMessage
import no.nav.ufore.inntektsplanleggeren.inntektsplanlegger.validation.InntektsplanleggerMessageCode
import no.nav.ufore.inntektsplanleggeren.inntektsplanlegger.validation.Validator
import no.nav.ufore.inntektsplanleggeren.pensjon.PenClient
import no.nav.ufore.inntektsplanleggeren.pensjon.dto.Uforetrygd
import no.nav.ufore.inntektsplanleggeren.util.NowProvider
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.time.LocalDate
import java.time.Month
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import no.nav.ufore.inntektsplanleggeren.inntekt.model.*
import no.nav.ufore.inntektsplanleggeren.pensjon.dto.Inntektsgrunnlag
import org.mockito.Mockito

class StartsideServiceTest {
    private val validator = mock(Validator::class.java)
    private val inntektService = mock(InntektService::class.java)
    private val penClient = mock(PenClient::class.java)
    private val nowProvider = mock(NowProvider::class.java)
    private val inntektsplanleggerService = mock(InntektsplanleggerService::class.java)

    private val startsideService = StartsideService(penClient, validator, inntektService, inntektsplanleggerService, nowProvider)

    @BeforeEach
    fun setup() {
        MockitoAnnotations.openMocks(this)
        `when`(nowProvider.now()).thenReturn(LocalDate.now())
    }

    @Test
    fun `hentStartsideData skal returnere StartsideData fra uforetrygd, valideringsmeldinger og forventede inntekter`() {
        val year = LocalDate.now().year

        val expectedForventetInntektBruker = mapOf(year to 5000)
        val expectedForventetInntektEps = mapOf(year to 6000)
        val expectedInntektsgrense = 232
        val expectedKompensasjonsgrad = 23.2
        val expectedGrenseStoppAvUfoeretrygd = 564654

        val uforetrygd = uforetrygd(
            inntektsgrense = expectedInntektsgrense,
            kompensasjonsgrad = expectedKompensasjonsgrad,
            grenseStoppAvUfoeretrygd = expectedGrenseStoppAvUfoeretrygd,
            barnetilleggFellesbarn = true,
        )

        `when`(penClient.fetchInntektsplanleggerData(PID, LocalDate.now().plusMonths(1).withDayOfMonth(1))).thenReturn(
            uforetrygd
        )

        `when`(inntektService.getForventedeInntekter(PID, uforetrygd, year)).thenReturn(
            ForventedeInntekterSummary(
                ForventedeInntekter(
                    PersonInntekter(null, null, null, null, null),
                    null
                ),
                expectedForventetInntektBruker[year]!!,
                expectedForventetInntektEps[year]!!
            )
        )

        `when`(inntektsplanleggerService.getSimuleringFomDato(year)).thenReturn(LocalDate.now().plusMonths(1).withDayOfMonth(1))
        `when`(inntektsplanleggerService.finnAarKanRegistrereFor(false, false)).thenReturn(listOf(year))
        `when`(validator.validateUserInitialData(any(), any())).thenReturn(emptyList())

        val startsideData = startsideService.hentStartsideData(PID, year)

        assertEquals(expectedForventetInntektBruker, startsideData.uforetrygd!!.forventetInntekt[year]?.let { mapOf(year to it) })
        assertEquals(expectedForventetInntektEps, startsideData.uforetrygd.forventetInntektAnnenForelder[year]?.let { mapOf(year to it) })
        assertEquals(expectedInntektsgrense, startsideData.uforetrygd.inntektsgrense)
        assertEquals(expectedKompensasjonsgrad, startsideData.uforetrygd.reduksjonsprosent)
        assertEquals(expectedGrenseStoppAvUfoeretrygd, startsideData.uforetrygd.inntektstak)
        assertTrue(startsideData.uforetrygd.harBarnetilleggFellesbarn)
        assertEquals(emptyList(), startsideData.messages)
    }

    @Test
    fun `hentStartsideData skal ikke sette StartsideData når validering feiler`(){
        val year = LocalDate.now().year
        val expectedMessages = listOf(InntektsplanleggerMessage(InntektsplanleggerMessageCode.FIELD_CAN_NOT_BE_NULL))

        val uforetrygd = uforetrygd()
        `when`(penClient.fetchInntektsplanleggerData(PID, LocalDate.now().plusMonths(1).withDayOfMonth(1))).thenReturn(
            uforetrygd
        )
        `when`(validator.validateUserInitialData(any(), any())).thenReturn(expectedMessages)

        val initialData = startsideService.hentStartsideData(PID, year)

        assertEquals(expectedMessages, initialData.messages)
        assertNull(initialData.uforetrygd)
    }

    @Test
    fun `getAnnetRelevantAar skal kun sette seTallForAar til dette år når det er desember`() {
        val år = LocalDate.now().year

        `when`(nowProvider.now()).thenReturn(LocalDate.now().withMonth(Month.DECEMBER.value))

        val annetRelevantÅrNårDesember = startsideService.getAnnetRelevantAar()
        assertEquals(år, annetRelevantÅrNårDesember)

        `when`(nowProvider.now()).thenReturn(LocalDate.now().withMonth(Month.JUNE.value))

        val annetRelevantÅrNårJuni = startsideService.getAnnetRelevantAar()
        assertEquals(null, annetRelevantÅrNårJuni)
    }

    @Test
    fun `hentStartsideData skal sette uføretrygd til null når uforetrygd er null`() {
        val year = LocalDate.now().year

        val uforetrygd = null
        `when`(penClient.fetchInntektsplanleggerData(PID, LocalDate.now().plusMonths(1).withDayOfMonth(1))).thenReturn(
            uforetrygd
        )
        `when`(validator.validateUserInitialData(any(), any())).thenReturn(emptyList())

        val initialData = startsideService.hentStartsideData(PID, year)
        assertNull(initialData.uforetrygd)
        assertTrue(initialData.messages.isEmpty())
    }

    @Test
    fun `hentStartsideData skal ikke sette uføretrygd til null når validering returner info eller warning`() {
        val year = LocalDate.now().year
        val expectedMessages = listOf(InntektsplanleggerMessage(
            InntektsplanleggerMessageCode.FORVENTET_INNTEKT_THIS_YEAR_USED_NEXT_YEAR_INFO),
            InntektsplanleggerMessage(InntektsplanleggerMessageCode.EPS_INNTEKT_CHANGED))


        val uforetrygd = uforetrygd()
        `when`(penClient.fetchInntektsplanleggerData(PID, LocalDate.now().plusMonths(1).withDayOfMonth(1))).thenReturn(
            uforetrygd
        )
        `when`(inntektService.getForventedeInntekter(PID, uforetrygd, year)).thenReturn(forventedeInntekterRegistrert())
        `when`(validator.validateUserInitialData(any(), any())).thenReturn(expectedMessages)
        `when`(nowProvider.now()).thenReturn(LocalDate.now().withMonth(Month.OCTOBER.value))
        `when`(inntektsplanleggerService.getSimuleringFomDato(year)).thenReturn(LocalDate.now().plusMonths(1).withDayOfMonth(1))
        `when`(inntektsplanleggerService.finnAarKanRegistrereFor(any(), any())).thenReturn(listOf(year))

        val initialData = startsideService.hentStartsideData(PID, year)
        assertNotNull(initialData.uforetrygd)
        assertEquals(expectedMessages, initialData.messages)
    }

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

    private fun <T> any(): T = Mockito.any()


    companion object {
        private const val PID = "00000000001"
    }
}