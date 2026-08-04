package no.nav.ufore.inntektsplanleggeren.inntektsplanlegger.validation

import no.nav.ufore.inntektsplanleggeren.inntekt.model.ForventedeInntekterSummary
import no.nav.ufore.inntektsplanleggeren.inntektsplanlegger.inntekt.ForventedeInntekter
import no.nav.ufore.inntektsplanleggeren.pensjon.dto.Uforetrygd
import no.nav.ufore.inntektsplanleggeren.util.NowProvider
import org.springframework.stereotype.Service
import java.time.Month

@Service
class Validator(
    private val inntektValidator: InntektValidator,
    private val nowProvider: NowProvider
) {
    fun validateUserInitialData(
        uforetrygd: Uforetrygd?,
        aktuelleAar: List<Int>
    ): List<InntektsplanleggerMessage> {
        if (uforetrygd == null) {
            return listOf(InntektsplanleggerMessage(InntektsplanleggerMessageCode.USER_HAS_NO_UFORE))
        }

        if (!uforetrygd.hasLopendeUforeVedtakNextYear && !uforetrygd.hasLopendeUforeVedtakThisYear) {
            return listOf(InntektsplanleggerMessage(InntektsplanleggerMessageCode.USER_HAS_NO_LOPENDE_VEDTAK_YET))
        }

        return validateAktuelleAar(aktuelleAar)
    }

    fun validateUserAndInputBeforeSimulering(
        uforetrygd: Uforetrygd?,
        oppgitteForventedeInntekter: ForventedeInntekter,
        registrerteForventedeInntekter: ForventedeInntekterSummary?,
        pid: String,
        simuleringsaar: Int,
        aktuelleAar: List<Int>
    ): List<InntektsplanleggerMessage> {
        val monthValidation = validateMonth(simuleringsaar)
        if (monthValidation != null) {
            return listOf(monthValidation)
        }
        val validationMessages = mutableListOf<InntektsplanleggerMessage>()
        validationMessages.addAll(validateUserInitialData(uforetrygd, aktuelleAar))

        if (!aktuelleAar.contains(simuleringsaar)) {
            validationMessages.add(InntektsplanleggerMessage(InntektsplanleggerMessageCode.ILLEGAL_SIMULERINGSAAR))
        }

        if (uforetrygd != null) {
            validationMessages.addAll(
                inntektValidator.validateInntekter(
                    pid,
                    oppgitteForventedeInntekter,
                    uforetrygd,
                    registrerteForventedeInntekter,
                    simuleringsaar
                )
            )
        }

        return validationMessages
    }

    private fun validateAktuelleAar(aktuelleAar: List<Int>): List<InntektsplanleggerMessage>{
        val messages = mutableListOf<InntektsplanleggerMessage>()
        val thisYear = nowProvider.now().year

        if (aktuelleAar.contains(thisYear + 1)) {
            messages.add(InntektsplanleggerMessage(InntektsplanleggerMessageCode.FORVENTET_INNTEKT_THIS_YEAR_USED_NEXT_YEAR_INFO))
            if (!aktuelleAar.contains(thisYear)) {
                messages.add(InntektsplanleggerMessage(InntektsplanleggerMessageCode.CAN_NOT_REPORT_INNTEKT_FOR_THIS_YEAR))
            }
        }
        return messages
    }

    private fun validateMonth(simuleringsaar: Int): InntektsplanleggerMessage? {
        val today = nowProvider.now()
        if (today.year == simuleringsaar && today.month == Month.DECEMBER) {
            return InntektsplanleggerMessage(InntektsplanleggerMessageCode.ILLEGAL_MONTH_DECEMBER_THIS_YEAR)
        }
        return null
    }
}