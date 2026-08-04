package no.nav.ufore.inntektsplanleggeren.inntektsplanlegger.validation

import no.nav.ufore.inntektsplanleggeren.inntektsplanlegger.inntekt.ForventedeInntekter
import no.nav.ufore.inntektsplanleggeren.inntektsplanlegger.inntekt.PersonInntekter
import no.nav.ufore.inntektsplanleggeren.pensjon.dto.Uforetrygd
import no.nav.ufore.inntektsplanleggeren.util.NowProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.time.LocalDate
import java.time.Month

class ValidatorTest {
    private val inntektValidator = mock(InntektValidator::class.java)
    private val nowProvider = mock(NowProvider::class.java)
    private val validator = Validator(inntektValidator, nowProvider)
    private val thisYear = LocalDate.now().year

    @Test
    fun `should return USER_HAS_NO_UFORE when uforetrygd is null`() {
        assertEquals(
            InntektsplanleggerMessageCode.USER_HAS_NO_UFORE,
            validator.validateUserInitialData(null, listOf(thisYear))[0].messageCode
        )
    }

    @Test
    fun `should return USER_HAS_NO_LOPENDE_VEDTAK_YET no lopende uforevedtak this year and next year`() {
        val result = validator.validateUserInitialData(
            uforetrygd(false, false),
            listOf(thisYear)
        )
        assertEquals(InntektsplanleggerMessageCode.USER_HAS_NO_LOPENDE_VEDTAK_YET, result[0].messageCode)
    }

    @Test
    fun `should return ILLEGAL_MONTH_DECEMBER_THIS_YEAR when trying to simulate current year in December`() {
        `when`(nowProvider.now()).thenReturn(LocalDate.now().withMonth(Month.DECEMBER.value))
        val result = validator.validateUserAndInputBeforeSimulering(
            uforetrygd(true, true), ForventedeInntekter(
                PersonInntekter(0, 0, 0, 0, 0), null
            ), null, "", thisYear, listOf(thisYear)
        )
        assertEquals(InntektsplanleggerMessageCode.ILLEGAL_MONTH_DECEMBER_THIS_YEAR, result[0].messageCode)
    }

    @Test
    fun `should not return ILLEGAL_MONTH_DECEMBER_THIS_YEAR when trying to simulate current year in another month than December`() {
        `when`(nowProvider.now()).thenReturn(LocalDate.now().withMonth(Month.NOVEMBER.value))
        val result = validator.validateUserAndInputBeforeSimulering(
            uforetrygd(true, true), ForventedeInntekter(
                PersonInntekter(0, 0, 0, 0, 0), null
            ), null, "", thisYear, listOf(thisYear)
        )
        assertTrue(result.isEmpty())
    }

    @Test
    fun `should validate initial and inntekt when validateUserAndInputBeforeSimulering`() {
        `when`(nowProvider.now()).thenReturn(LocalDate.now().withMonth(Month.NOVEMBER.value))
        `when`(inntektValidator.validateInntekter(any(), any(), any(), any(), anyInt())).thenReturn(
            listOf(
                InntektsplanleggerMessage(
                    InntektsplanleggerMessageCode.EPS_INNTEKT_CHANGED
                )
            )
        )
        val result = validator.validateUserAndInputBeforeSimulering(
            uforetrygd(false, false), ForventedeInntekter(
                PersonInntekter(0, 0, 0, 0, 0), null
            ), null, "", thisYear, listOf(thisYear)
        )
        assertTrue(result.any { InntektsplanleggerMessageCode.EPS_INNTEKT_CHANGED == it.messageCode })
        assertTrue(result.any { InntektsplanleggerMessageCode.USER_HAS_NO_LOPENDE_VEDTAK_YET == it.messageCode })
    }

    @Test
    fun `should return FORVENTET_INNTEKT_THIS_YEAR_USED_NEXT_YEAR_INFO only when this year and next year in aktuelleAar`() {
        `when`(nowProvider.now()).thenReturn(LocalDate.now())

        val messages = validator.validateUserInitialData(uforetrygd(true, true), listOf(thisYear, thisYear + 1))

        assertEquals(1, messages.size)
        assertEquals(InntektsplanleggerMessageCode.FORVENTET_INNTEKT_THIS_YEAR_USED_NEXT_YEAR_INFO, messages[0].messageCode)
        assertEquals(InntektsplanleggerMessageType.INFO, messages[0].type)
    }

    @Test
    fun `should return FORVENTET_INNTEKT_THIS_YEAR_USED_NEXT_YEAR_INFO and CAN_NOT_REPORT_INNTEKT_FOR_THIS_YEAR when only next year in aktuelleAar`() {
        `when`(nowProvider.now()).thenReturn(LocalDate.now())

        val messages = validator.validateUserInitialData(uforetrygd(true, true), listOf(thisYear + 1))

        assertEquals(2, messages.size)
        assertEquals(InntektsplanleggerMessageCode.FORVENTET_INNTEKT_THIS_YEAR_USED_NEXT_YEAR_INFO, messages[0].messageCode)
        assertEquals(InntektsplanleggerMessageType.INFO, messages[0].type)
        assertEquals(InntektsplanleggerMessageCode.CAN_NOT_REPORT_INNTEKT_FOR_THIS_YEAR, messages[1].messageCode)
        assertEquals(InntektsplanleggerMessageType.INFO, messages[1].type)
    }

    @Test
    fun `should not return FORVENTET_INNTEKT_THIS_YEAR_USED_NEXT_YEAR_INFO or CAN_NOT_REPORT_INNTEKT_FOR_THIS_YEAR when aktuelleAar is null`() {
        `when`(nowProvider.now()).thenReturn(LocalDate.now())

        val messages = validator.validateUserInitialData(uforetrygd(true, true), listOf(thisYear))

        assertTrue(messages.isEmpty())
    }

    @Test
    fun `should return ILLEGAL_SIMULERINGSAAR when simuleringsaar not in aktuelleAar`(){
        `when`(nowProvider.now()).thenReturn(LocalDate.now().withYear(2021).withDayOfYear(1))

        val messages = validator.validateUserAndInputBeforeSimulering(
            uforetrygd(true, false),
            ForventedeInntekter(PersonInntekter(null, null, null, null, null), null),
            null,
            "",
            2022,
            listOf(2020, 2021))

        assertEquals(1, messages.size)
        assertEquals(InntektsplanleggerMessageCode.ILLEGAL_SIMULERINGSAAR, messages[0].messageCode)
        assertEquals(InntektsplanleggerMessageType.ERROR, messages[0].type)
    }

    @Test
    fun `should not return ILLEGAL_SIMULERINGSAAR when simuleringsaar in aktuelleAar`(){
        `when`(nowProvider.now()).thenReturn(LocalDate.now().withYear(2021).withDayOfYear(1))
        val messages = validator.validateUserAndInputBeforeSimulering(
            uforetrygd(true, false),
            ForventedeInntekter(PersonInntekter(null, null, null, null, null), null),
            null,
            "",
            2020,
            listOf(2020, 2021))
        assertTrue(messages.isEmpty())
    }

    private fun uforetrygd(hasLopendeVedtakThisYear: Boolean, hasLopvedtakNextYear: Boolean) =
        Uforetrygd(
            0,
            0.0,
            0,
            hasLopendeVedtakThisYear,
            hasLopvedtakNextYear,
            false,
            false,
            false,
            false,
            false,
            null,
            null,
            null,
            null
        )

    private fun <T> any(): T = Mockito.any()
}