package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.InntektService
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.ForventedeInntekter
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.ForventedeInntekterSummary
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.PersonInntekter
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.simulering.SimuleringService
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation.InntektsplanleggerMessage
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation.InntektsplanleggerMessageCode
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation.Validator
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.PenClient
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.Inntektsgrunnlag
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.Pensjonsdata
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security.TokenService
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.util.NowProvider
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
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

    @Test
    fun `should return InntektsplanleggerInitialData from pensjonsdata, validation result and forventede inntekter when constructInitialInntektsplanleggerResponse`() {
        val year = LocalDate.now().year

        val expectedForventetInntektBruker = 5000
        val expectedForventetInntektEps = 6000
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
                expectedForventetInntektBruker,
                expectedForventetInntektEps
            )
        )
        `when`(validator.validateUserInitialData(pensjonsdata)).thenReturn(emptyList())

        val initialData = inntektsplanleggerService.constructInitialInntektsplanleggerResponse(PID, year)

        assertEquals(expectedForventetInntektBruker, initialData.data!!.forventetInntekt)
        assertEquals(expectedForventetInntektEps, initialData.data.forventetInntektAnnenForelder)
        assertEquals(expectedInntektsgrense, initialData.data.inntektsgrense)
        assertEquals(expectedKompensasjonsgrad, initialData.data.kompensasjonsgrad)
        assertEquals(expectedGrenseStoppAvUfoeretrygd, initialData.data.grenseStoppAvUfoeretrygd)
        assertEquals(expectedGrenseStoppAvBarnetilleggFellesbarn, initialData.data.grenseStoppAvBarnetilleggFellesbarn)
        assertEquals(
            expectedGrenseStoppAvBarnetilleggSaerkullsbarn,
            initialData.data.grenseStoppAvBarnetilleggSaerkullsbarn
        )
        assertEquals(expectedFribelopFellesbarn, initialData.data.fribelopBarnetilleggFellesbarn)
        assertEquals(expectedFribelopSaerkullsbarn, initialData.data.fribelopBarnetilleggSaerkullsbarn)
        assertTrue(initialData.data.hasGjenlevendeTillegg)
        assertTrue(initialData.data.hasVarigTilrettelagtArbeid)
        assertFalse(initialData.data.hasBarnetilleggSaerkullsbarn)
        assertTrue(initialData.data.hasBarneTilleggFellesbarn)
    }

    @Test
    fun `should set aktuelleAar to current year only when before october and hasLopendeUforeVedtakThisYear`() {
        val year = LocalDate.now().year

        val pensjonsdata = pensjonsdata(
            hasLopendeUforeVedtakThisYear = true,
            hasLopendeUforeVedtakNextYear = false
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
                0,
                0
            )
        )
        `when`(validator.validateUserInitialData(pensjonsdata)).thenReturn(emptyList())
        `when`(nowProvider.now()).thenReturn(LocalDate.now().withMonth(Month.SEPTEMBER.value))

        val initialData = inntektsplanleggerService.constructInitialInntektsplanleggerResponse(PID, year)
        assertEquals(1, initialData.data!!.aktuelleAar.size)
        assertEquals(year, initialData.data.aktuelleAar[0])
    }

    @Test
    fun `should set aktuelleAar to current year and next year when after october and hasLopendeUforeVedtakThisYear`() {
        val year = LocalDate.now().year

        val pensjonsdata = pensjonsdata(
            hasLopendeUforeVedtakThisYear = true,
            hasLopendeUforeVedtakNextYear = false
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
                0,
                0
            )
        )
        `when`(validator.validateUserInitialData(pensjonsdata)).thenReturn(emptyList())
        `when`(nowProvider.now()).thenReturn(LocalDate.now().withMonth(Month.OCTOBER.value))

        val initialData = inntektsplanleggerService.constructInitialInntektsplanleggerResponse(PID, year)
        assertEquals(2, initialData.data!!.aktuelleAar.size)
        assertEquals(year, initialData.data.aktuelleAar[0])
        assertEquals(year + 1, initialData.data.aktuelleAar[1])
    }

    @Test
    fun `should set aktuelleAar to next year only when after october and hasLopendeUforeVedtakNextYear`() {
        val year = LocalDate.now().year

        val pensjonsdata = pensjonsdata(
            hasLopendeUforeVedtakThisYear = false,
            hasLopendeUforeVedtakNextYear = true
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
                0,
                0
            )
        )
        `when`(validator.validateUserInitialData(pensjonsdata)).thenReturn(emptyList())
        `when`(nowProvider.now()).thenReturn(LocalDate.now().withMonth(Month.OCTOBER.value))

        val initialData = inntektsplanleggerService.constructInitialInntektsplanleggerResponse(PID, year)
        assertEquals(1, initialData.data!!.aktuelleAar.size)
        assertEquals(year + 1, initialData.data.aktuelleAar[0])
    }

    @Test
    fun `should set data to null when validation returns error`(){
        val year = LocalDate.now().year
        val expectedMessages = listOf(InntektsplanleggerMessage(InntektsplanleggerMessageCode.USER_HAS_NO_UFORE))

        val pensjonsdata = pensjonsdata()
        `when`(penClient.fetchInntektsplanleggerData(PID, LocalDate.now().plusMonths(1).withDayOfMonth(1))).thenReturn(
            pensjonsdata
        )
        `when`(inntektService.getForventedeInntekter(PID, pensjonsdata, year)).thenReturn(
            ForventedeInntekterSummary(
                ForventedeInntekter(
                    PersonInntekter(null, null, null, null, null),
                    null
                ),
                0,
                0
            )
        )
        `when`(validator.validateUserInitialData(pensjonsdata)).thenReturn(expectedMessages)
        `when`(nowProvider.now()).thenReturn(LocalDate.now().withMonth(Month.OCTOBER.value))

        val initialData = inntektsplanleggerService.constructInitialInntektsplanleggerResponse(PID, year)
        assertNull(initialData.data)
        assertEquals(expectedMessages, initialData.messages)
    }

    @Test
    fun `should return InntekterResponse from inntektskomponent data when constructInntekterResponse`() {

    }

    @Test
    fun `should return SimuleringResponse with simuleringsresultat when validation is OK`() {

    }

    @Test
    fun `should return SimuleringResponse without simuleringsresultat when validation returns ERROR`() {

    }

    @Test
    fun `should return status AUTOMATISK_BEHANDLING when AUTOMATISK_BEHANDLING status from innsending in PEN`() {

    }

    @Test
    fun `should return status INNTEKT_LAGRET_INGEN_BEHANDLING when INNTEKT_LAGRET status from innsending in PEN`() {

    }

    @Test
    fun `should return status IKKE_SENDT when PEN returns unknown status`() {

    }

    @Test
    fun `should return status IKKE_SENDT_VALIDERING_FEILET when validation is failing before innsending to PEN`() {

    }

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
            barnetilleggFellesbarn = barnetilleggFellesbarn,
            barnetilleggSaerkullsbarn = barnetilleggSaerkullsbarn,
            grenseStoppAvBarnetilleggFellesbarn = grenseStoppAvBarnetilleggFellesbarn,
            grenseStoppAvBarnetilleggSaerkullsbarn = grenseStoppAvBarnetilleggSaerkullsbarn,
            fribelopBarnetilleggSaerkullsbarn = fribelopBarnetilleggSaerkullsbarn,
            fribelopBarnetilleggFellesbarn = fribelopBarnetilleggFellesbarn,
            epsPid = epsPid,
            inntekterFromOpenKravBruker = inntekterFromOpenKravBruker,
            inntekterFromOpenKravEps = inntekterFromOpenKravEps
        )

    private fun <T> any(): T = Mockito.any()

    companion object {
        private const val PID = "00000000001"
    }
}