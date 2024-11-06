package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.inntekt.ForventedeInntekter
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.inntekt.PersonInntekter
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.simulering.SimuleringResponse
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.SimulerEndringUforetrygdResponse
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.UforetrygdSummary
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.UforetrygdYtelseskomponenter
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.Ytelseskomponent
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.person.PersonService
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.time.LocalDate
import kotlin.test.assertEquals

class SimuleringValidatorTest {

    private val personService = mock(PersonService::class.java)
    private val simuleringValidator = SimuleringValidator(personService)

    @Test
    fun `should return OPPGITT_INNTEKT_OVER_INNTEKTSTAK when sum arbeidsinntekt inntekt utland and naeringsinntekt is more than inntektstak`() {
        val simuleringsresultat = simuleringsresultat(inntektstak = 200000)

        val forventedeInntekter = ForventedeInntekter(
            bruker = PersonInntekter(
                arbeidsinntekt = 100000,
                naeringsinntekt = 50000,
                inntektUtland = 50001,
                andrePensjonsgivendeYtelser = null,
                pensjonUtland = null
            ),
            eps = null
        )

        val result = simuleringValidator.validateSimulering(
            simuleringsresultat,
            forventedeInntekter,
            LocalDate.now().plusMonths(1).withDayOfMonth(1),
            LocalDate.now().year,
            PID
        )
        assertEquals(InntektsplanleggerMessageCode.OPPGITT_INNTEKT_OVER_INNTEKTSTAK, result[0].messageCode)
        assertEquals(200000, result[0].metadata[MetadataKey.INNTEKTSTAK])
        assertEquals(200001, result[0].metadata[MetadataKey.SUM_OVER_INNTEKTSTAK])
    }

    @Test
    fun `should not return OPPGITT_INNTEKT_OVER_INNTEKTSTAK when sum arbeidsinntekt inntekt utland and naeringsinntekt is same as inntektstak`() {
        val simuleringsresultat = simuleringsresultat(inntektstak = 200000)

        val forventedeInntekter = ForventedeInntekter(
            bruker = PersonInntekter(
                arbeidsinntekt = 100000,
                naeringsinntekt = 50000,
                inntektUtland = 50000,
                andrePensjonsgivendeYtelser = null,
                pensjonUtland = null
            ),
            eps = null
        )

        val result = simuleringValidator.validateSimulering(
            simuleringsresultat,
            forventedeInntekter,
            LocalDate.now().plusMonths(1).withDayOfMonth(1),
            LocalDate.now().year,
            PID
        )
        assertTrue(result.isEmpty())
    }

    @Test
    fun `should return OPPGITT_INNTEKT_GIVES_LOWER_UFORETRYGD_THAN_ALREADY_UTBETALT when simulering result gives lower uforetrygd than current`() {
        val sumHittilIAar = 200000
        val simulertSum = 199999
        val simuleringsresultat = simuleringsresultat(sumHittilUtbetaltIAr = sumHittilIAar, simulertSum = simulertSum)

        val forventedeInntekter = forventedeInntekter()

        val result = simuleringValidator.validateSimulering(
            simuleringsresultat,
            forventedeInntekter,
            LocalDate.now().plusMonths(1).withDayOfMonth(1),
            LocalDate.now().year,
            PID
        )
        assertEquals(
            InntektsplanleggerMessageCode.OPPGITT_INNTEKT_GIVES_LOWER_UFORETRYGD_THAN_ALREADY_UTBETALT,
            result[0].messageCode
        )
        assertEquals(
            sumHittilIAar,
            result[0].metadata[MetadataKey.UFORE_HITTIL_I_AR]
        )
        assertEquals(
            simulertSum,
            result[0].metadata[MetadataKey.SUM_SIMULERT_UFORETRYGD]
        )
    }

    @Test
    fun `should not return OPPGITT_INNTEKT_GIVES_LOWER_UFORETRYGD_THAN_ALREADY_UTBETALT when simulering result gives same uforetrygd as current`() {
        val sumHittilIAar = 200000
        val simulertSum = 200000
        val simuleringsresultat = simuleringsresultat(sumHittilUtbetaltIAr = sumHittilIAar, simulertSum = simulertSum)

        val forventedeInntekter = forventedeInntekter()

        val result = simuleringValidator.validateSimulering(
            simuleringsresultat,
            forventedeInntekter,
            LocalDate.now().plusMonths(1).withDayOfMonth(1),
            LocalDate.now().year,
            PID
        )
        assertTrue(result.isEmpty())
    }

    @Test
    fun `should return OPPGITT_INNTEKT_GIVES_MORE_UFORETRYGD_THAN_ALREADY_UTBETALT when simulert rest i aar is larger than current rest i aar`() {
        val sumBruttoRestArWithoutBTandET = 200000.0
        val sumNettoRestArWithoutBTandET = 300000

        `when`(personService.getFodselsdato(PID)).thenReturn(LocalDate.now().plusYears(2))

        val simuleringsresultat = simuleringsresultat(
            sumBruttoRestArWithoutBTandET = sumBruttoRestArWithoutBTandET,
            sumNettoRestArWithoutBTandET = sumNettoRestArWithoutBTandET
        )

        val forventedeInntekter = forventedeInntekter()

        val result = simuleringValidator.validateSimulering(
            simuleringsresultat,
            forventedeInntekter,
            LocalDate.now().plusMonths(1).withDayOfMonth(1),
            LocalDate.now().year,
            PID
        )
        assertEquals(
            InntektsplanleggerMessageCode.OPPGITT_INNTEKT_GIVES_MORE_UFORETRYGD_THAN_ALREADY_UTBETALT,
            result[0].messageCode
        )
    }

    @Test
    fun `should not return OPPGITT_INNTEKT_GIVES_MORE_UFORETRYGD_THAN_ALREADY_UTBETALT when simulering gives less uforetrygd than current`() {
        val sumBruttoRestArWithoutBTandET = 300000.0
        val sumNettoRestArWithoutBTandET = 20000

        `when`(personService.getFodselsdato(PID)).thenReturn(LocalDate.now().plusYears(2))

        val simuleringsresultat = simuleringsresultat(
            sumBruttoRestArWithoutBTandET = sumBruttoRestArWithoutBTandET,
            sumNettoRestArWithoutBTandET = sumNettoRestArWithoutBTandET
        )

        val forventedeInntekter = forventedeInntekter()

        val result = simuleringValidator.validateSimulering(
            simuleringsresultat,
            forventedeInntekter,
            LocalDate.now().plusMonths(1).withDayOfMonth(1),
            LocalDate.now().year,
            PID
        )
        assertTrue(result.isEmpty())
    }

    @Test
    fun `should not return OPPGITT_INNTEKT_GIVES_MORE_UFORETRYGD_THAN_ALREADY_UTBETALT when simulering gives less uforetrygd than current with tomDato first in month after 67`() {
        val sumBruttoRestArWithoutBTandET = 60000.0
        val sumNettoRestArWithoutBTandET = 20000
        val simuleringFom = LocalDate.of(2023,3,1)
        val firstInMonthAfter67 = simuleringFom.plusMonths(4)

        `when`(personService.getFodselsdato(PID)).thenReturn(firstInMonthAfter67.minusYears(67).minusMonths(1))

        val simuleringsresultat = simuleringsresultat(
            sumBruttoRestArWithoutBTandET = sumBruttoRestArWithoutBTandET,
            sumNettoRestArWithoutBTandET = sumNettoRestArWithoutBTandET
        )

        val forventedeInntekter = forventedeInntekter()

        val result = simuleringValidator.validateSimulering(
            simuleringsresultat,
            forventedeInntekter,
            simuleringFom,
            simuleringFom.year,
            PID
        )
        assertTrue(result.isEmpty())
    }

    @Test
    fun `should return OPPGITT_INNTEKT_GIVES_MORE_UFORETRYGD_THAN_ALREADY_UTBETALT when simulering gives less uforetrygd than current with tomDato first in month after 67`() {
        val sumBruttoRestArWithoutBTandET = 60000.0
        val sumNettoRestArWithoutBTandET = 20001
        val simuleringFom = LocalDate.of(2023,3,1)
        val firstInMonthAfter67 = simuleringFom.plusMonths(4)

        `when`(personService.getFodselsdato(PID)).thenReturn(firstInMonthAfter67.minusYears(67).minusMonths(1))

        val simuleringsresultat = simuleringsresultat(
            sumBruttoRestArWithoutBTandET = sumBruttoRestArWithoutBTandET,
            sumNettoRestArWithoutBTandET = sumNettoRestArWithoutBTandET
        )

        val forventedeInntekter = forventedeInntekter()

        val result = simuleringValidator.validateSimulering(
            simuleringsresultat,
            forventedeInntekter,
            simuleringFom,
            simuleringFom.year,
            PID
        )
        assertEquals(
            InntektsplanleggerMessageCode.OPPGITT_INNTEKT_GIVES_MORE_UFORETRYGD_THAN_ALREADY_UTBETALT,
            result[0].messageCode
        )
    }

    @Test
    fun `should return FAKTOROMREGNET_ELLER_MANUELT_OVERSTYRT when simulering is faktoromregnet or manuelt overstyrt`() {
        val simuleringsresultat = simuleringsresultat(isFaktoromregnetEllerManueltOverstyrt = true)

        val forventedeInntekter = forventedeInntekter()

        val result = simuleringValidator.validateSimulering(
            simuleringsresultat,
            forventedeInntekter,
            LocalDate.now().plusMonths(1).withDayOfMonth(1),
            LocalDate.now().year,
            PID
        )
        assertEquals(
            InntektsplanleggerMessageCode.FAKTOROMREGNET_ELLER_MANUELT_OVERSTYRT,
            result[0].messageCode
        )
    }

    @Test
    fun `should not return FAKTOROMREGNET_ELLER_MANUELT_OVERSTYRT when simulering is not faktoromregnet or manuelt overstyrt`() {
        val simuleringsresultat = simuleringsresultat(isFaktoromregnetEllerManueltOverstyrt = false)

        val forventedeInntekter = forventedeInntekter()

        val result = simuleringValidator.validateSimulering(
            simuleringsresultat,
            forventedeInntekter,
            LocalDate.now().plusMonths(1).withDayOfMonth(1),
            LocalDate.now().year,
            PID
        )
        assertTrue(result.isEmpty())
    }

    @Test
    fun `should return OPEN_INNTEKTSENDRING_KRAV when there exists an open krav for inntektsendring`() {
        val simuleringsresultat = simuleringsresultat(hasOpenInntektsendringskrav = true)

        val forventedeInntekter = forventedeInntekter()

        val result = simuleringValidator.validateSimulering(
            simuleringsresultat,
            forventedeInntekter,
            LocalDate.now().plusMonths(1).withDayOfMonth(1),
            LocalDate.now().year,
            PID
        )
        assertEquals(
            InntektsplanleggerMessageCode.OPEN_INNTEKTSENDRING_KRAV,
            result[0].messageCode
        )
    }

    @Test
    fun `should not return OPEN_INNTEKTSENDRING_KRAV when there does not exist an open krav for inntektsendring`() {
        val simuleringsresultat = simuleringsresultat(hasOpenInntektsendringskrav = false)

        val forventedeInntekter = forventedeInntekter()

        val result = simuleringValidator.validateSimulering(
            simuleringsresultat,
            forventedeInntekter,
            LocalDate.now().plusMonths(1).withDayOfMonth(1),
            LocalDate.now().year,
            PID
        )
        assertTrue(result.isEmpty())
    }

    @Test
    fun `should return SIMULERING_CONTAINS_MOTREGNING when simulering contains motregning`() {
        val simuleringsresultat = simuleringsresultat(containsMotregning = true)

        val forventedeInntekter = forventedeInntekter()

        val result = simuleringValidator.validateSimulering(
            simuleringsresultat,
            forventedeInntekter,
            LocalDate.now().plusMonths(1).withDayOfMonth(1),
            LocalDate.now().year,
            PID
        )
        assertEquals(
            InntektsplanleggerMessageCode.SIMULERING_CONTAINS_MOTREGNING,
            result[0].messageCode
        )
    }

    @Test
    fun `should not return SIMULERING_CONTAINS_MOTREGNING when simulering does not contain motregning`() {
        val simuleringsresultat = simuleringsresultat(containsMotregning = false)

        val forventedeInntekter = forventedeInntekter()

        val result = simuleringValidator.validateSimulering(
            simuleringsresultat,
            forventedeInntekter,
            LocalDate.now().plusMonths(1).withDayOfMonth(1),
            LocalDate.now().year,
            PID
        )
        assertTrue(result.isEmpty())
    }

    private fun forventedeInntekter() =
        ForventedeInntekter(
            bruker = PersonInntekter(
                arbeidsinntekt = 100000,
                naeringsinntekt = 50000,
                inntektUtland = 50001,
                andrePensjonsgivendeYtelser = 0,
                pensjonUtland = 0
            ),
            eps = PersonInntekter(
                arbeidsinntekt = 3443,
                naeringsinntekt = 0,
                inntektUtland = 0,
                andrePensjonsgivendeYtelser = 0,
                pensjonUtland = 0
            )
        )

    private fun simuleringsresultat(
        sumHittilUtbetaltIAr: Int = 130000,
        simulertSum: Int = 350000,
        inntektstak: Int = 500000,
        sumBruttoRestArWithoutBTandET: Double? = null,
        sumNettoRestArWithoutBTandET: Int? = null,
        lastVedtakTom: LocalDate? = null,
        isFaktoromregnetEllerManueltOverstyrt: Boolean = false,
        hasOpenInntektsendringskrav: Boolean = false,
        containsMotregning: Boolean = false
    ) = SimulerEndringUforetrygdResponse(
        currentUforetrygdSummary = UforetrygdSummary(
            uforetrygdYtelseskomponenter = UforetrygdYtelseskomponenter(
                uforetrygdOrdiner = Ytelseskomponent(amountPerYear = 120000, amountPerMonth = 10000),
                barnetilleggFellesbarn = null,
                barnetilleggSaerkullsbarn = null,
                ektefelletillegg = null,
                gjenlevendetillegg = null
            ),
            totalbelopNettoAr = null,
            totalbelopNetto = null,
            sumYtelseskomponenter = null
        ),
        simulertUforetrygdSummary = UforetrygdSummary(
            uforetrygdYtelseskomponenter = UforetrygdYtelseskomponenter(
                uforetrygdOrdiner = Ytelseskomponent(amountPerYear = 120000, amountPerMonth = 10000),
                barnetilleggFellesbarn = null,
                barnetilleggSaerkullsbarn = null,
                ektefelletillegg = null,
                gjenlevendetillegg = null
            ),
            totalbelopNettoAr = null,
            totalbelopNetto = null,
            sumYtelseskomponenter = simulertSum
        ),
        containsMotregning = containsMotregning,
        sumHittilUtbetaltIAr = sumHittilUtbetaltIAr,
        sumNettoRestArWithoutBTandET = sumNettoRestArWithoutBTandET,
        sumBruttoRestArWithoutBTandET = sumBruttoRestArWithoutBTandET,
        inntektstak = inntektstak,
        gjeldendeBeregningFom = null,
        isFaktoromregnetEllerManueltOverstyrt = isFaktoromregnetEllerManueltOverstyrt,
        hasOpenInntektsendringskrav = hasOpenInntektsendringskrav,
        firstVedtakFom = LocalDate.now().withDayOfMonth(1),
        lastVedtakTom = lastVedtakTom
    )

    companion object {
        private const val PID = "00000000001"
    }
}