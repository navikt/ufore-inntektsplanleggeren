package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.ForventedeInntekterSummary
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.inntekt.ForventedeInntekter
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.Pensjonsdata
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.util.NowProvider
import org.springframework.stereotype.Service
import java.time.Month

@Service
class Validator(
    private val inntektValidator: InntektValidator,
    private val nowProvider: NowProvider
) {
    fun validateUserInitialData(
        pensjonsdata: Pensjonsdata?,
        aktuelleAar: List<Int>? = null
    ): List<InntektsplanleggerMessage> {
        if (pensjonsdata == null) {
            return listOf(InntektsplanleggerMessage(InntektsplanleggerMessageCode.USER_HAS_NO_UFORE))
        }

        if (!pensjonsdata.hasLopendeUforeVedtakNextYear && !pensjonsdata.hasLopendeUforeVedtakThisYear) {
            return listOf(InntektsplanleggerMessage(InntektsplanleggerMessageCode.USER_HAS_NO_LOPENDE_VEDTAK_YET))
        }

        val thisYear = nowProvider.now().year

        if (aktuelleAar != null && aktuelleAar.contains(thisYear + 1)) {
            if (!aktuelleAar.contains(thisYear)) {
                return listOf(
                    InntektsplanleggerMessage(InntektsplanleggerMessageCode.FORVENTET_INNTEKT_THIS_YEAR_USED_NEXT_YEAR_INFO),
                    InntektsplanleggerMessage(InntektsplanleggerMessageCode.CAN_NOT_REPORT_INNTEKT_FOR_THIS_YEAR)
                )
            }
            return listOf(InntektsplanleggerMessage(InntektsplanleggerMessageCode.FORVENTET_INNTEKT_THIS_YEAR_USED_NEXT_YEAR_INFO))
        }
        return emptyList()
    }

    fun validateUserAndInputBeforeSimulering(
        pensjonsdata: Pensjonsdata?,
        oppgitteForventedeInntekter: ForventedeInntekter,
        registrerteForventedeInntekter: ForventedeInntekterSummary?,
        pid: String,
        simuleringsaar: Int
    ): List<InntektsplanleggerMessage> {
        val monthValidation = validateMonth(simuleringsaar)
        if (monthValidation != null) {
            return listOf(monthValidation)
        }
        val validationMessages = mutableListOf<InntektsplanleggerMessage>()
        validationMessages.addAll(validateUserInitialData(pensjonsdata))
        if (pensjonsdata != null) {
            validationMessages.addAll(
                inntektValidator.validateInntekter(
                    pid,
                    oppgitteForventedeInntekter,
                    pensjonsdata,
                    registrerteForventedeInntekter,
                    simuleringsaar
                )
            )
        }


        return validationMessages
    }

    private fun validateMonth(simuleringsaar: Int): InntektsplanleggerMessage? {
        val today = nowProvider.now()
        if (today.year == simuleringsaar && today.month == Month.DECEMBER) {
            return InntektsplanleggerMessage(InntektsplanleggerMessageCode.ILLEGAL_MONTH_DECEMBER_THIS_YEAR)
        }
        return null
    }
}