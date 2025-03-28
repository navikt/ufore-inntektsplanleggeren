package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.inntekt.ForventedeInntekter
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.SimulerEndringUforetrygdResponse
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.person.PersonService
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.Month
import java.time.temporal.ChronoUnit

@Service
class SimuleringValidator(val personService: PersonService) {
    fun validateSimulering(
        simuleringsresultat: SimulerEndringUforetrygdResponse,
        forventedeInntekterOppgitt: ForventedeInntekter,
        simulerFomDato: LocalDate,
        simuleringsaar: Int,
        pid: String
    ): List<InntektsplanleggerMessage> {
        val messages = mutableListOf<InntektsplanleggerMessage>()
        validateInntektstak(simuleringsresultat, forventedeInntekterOppgitt)?.let { messages.add(it) }
            ?: validateTooMuchUtbetaltIAr(simuleringsresultat)?.let { messages.add(it) }

        validateTooLittleUtbetaltIAar(simuleringsresultat, simulerFomDato, simuleringsaar, pid)?.let { messages.add(it) }

        validateFaktoromregnetEllerManueltOverstyrt(simuleringsresultat)?.let { messages.add(it) }

        validateOpenInntektskrav(simuleringsresultat)?.let { messages.add(it) }

        validateMotregning(simuleringsresultat)?.let { messages.add(it) }

        return messages
    }

    private fun validateInntektstak(
        simuleringsresultat: SimulerEndringUforetrygdResponse,
        forventedeInntekterOppgitt: ForventedeInntekter
    ): InntektsplanleggerMessage? {
        val sumRelevantInnntekter = forventedeInntekterOppgitt.bruker.sumInntekterRelevantTowardsInntektstak()
        val inntektstak = simuleringsresultat.inntektstak
        if (inntektstak != null && sumRelevantInnntekter > inntektstak) {
            return InntektsplanleggerMessage(
                messageCode = InntektsplanleggerMessageCode.OPPGITT_INNTEKT_OVER_INNTEKTSTAK,
                metadata = mapOf(
                    MetadataKey.INNTEKTSTAK to inntektstak,
                    MetadataKey.SUM_OVER_INNTEKTSTAK to sumRelevantInnntekter
                )
            )
        }
        return null
    }

    private fun validateTooMuchUtbetaltIAr(simuleringsresultat: SimulerEndringUforetrygdResponse): InntektsplanleggerMessage? {
        val sumHittilIAr = simuleringsresultat.sumHittilUtbetaltIAr
        val sumYtelseskomponenter = simuleringsresultat.simulertUforetrygdSummary.sumYtelseskomponenter

        if (sumHittilIAr != null
            && sumYtelseskomponenter != null
            && sumHittilIAr > sumYtelseskomponenter
        ) {
            return InntektsplanleggerMessage(
                messageCode = InntektsplanleggerMessageCode.OPPGITT_INNTEKT_GIVES_LOWER_UFORETRYGD_THAN_ALREADY_UTBETALT,
                metadata = mapOf(
                    MetadataKey.UFORE_HITTIL_I_AR to sumHittilIAr,
                    MetadataKey.SUM_SIMULERT_UFORETRYGD to sumYtelseskomponenter
                )
            )
        }
        return null
    }

    private fun validateTooLittleUtbetaltIAar(
        simuleringsresultat: SimulerEndringUforetrygdResponse,
        simuleringFomDato: LocalDate,
        simuleringsaar: Int,
        pid: String
    ): InntektsplanleggerMessage? {
        if (simuleringsresultat.sumBruttoRestArWithoutBTandET == null || simuleringsresultat.sumNettoRestArWithoutBTandET == null) {
            return null
        }
        val maksimaltGjenstaendeIAr = calculateMaksimaltTilUtbetalingGjenstaendeIAr(
            simuleringsresultat.sumBruttoRestArWithoutBTandET.toDouble(),
            simuleringFomDato,
            simuleringsresultat.lastVedtakTom,
            pid,
            simuleringsaar
        )
        val ROUNDING_MARGIN = 2
        if (simuleringsresultat.sumNettoRestArWithoutBTandET > maksimaltGjenstaendeIAr+ROUNDING_MARGIN) {
            return InntektsplanleggerMessage(InntektsplanleggerMessageCode.OPPGITT_INNTEKT_GIVES_MORE_UFORETRYGD_THAN_ALREADY_UTBETALT)
        }


        return null
    }

    private fun calculateMaksimaltTilUtbetalingGjenstaendeIAr(
        sumBruttoRestArWithoutBTandET: Double,
        simuleringFomDato: LocalDate,
        lastVedtakTomDato: LocalDate?,
        pid: String,
        simuleringsaar: Int
    ): Int = Math.round(
        (sumBruttoRestArWithoutBTandET / 12) * monthsBetweenInSimuleringsaar(
            simuleringFomDato,
            getVedtakTomDato(lastVedtakTomDato, pid, simuleringsaar),
            simuleringsaar
        )
    ).toInt()


    private fun getVedtakTomDato(lastVedtakTomDato: LocalDate?, pid: String, simuleringsaar: Int): LocalDate? {
        val lastDayInMonthUserTurns67 = personService.getFodselsdato(pid)
            .plusYears(67)
            .plusMonths(1)
            .withDayOfMonth(1)
            .minusDays(1)
        if (lastVedtakTomDato == null && lastDayInMonthUserTurns67.year == simuleringsaar || lastVedtakTomDato != null && lastDayInMonthUserTurns67.isBefore(
                lastVedtakTomDato
            )
        ) {
            return lastDayInMonthUserTurns67
        }
        return lastVedtakTomDato
    }

    private fun monthsBetweenInSimuleringsaar(fomDato: LocalDate, tomDato: LocalDate?, simuleringsaar: Int): Int {
        val firstDayOfChosenYear = LocalDate.of(simuleringsaar, Month.JANUARY, 1)
        val lastDayOfChosenYear = LocalDate.of(simuleringsaar, Month.DECEMBER, 31)

        var gjeldendeFomDato = fomDato
        var gjeldendeTomDato = tomDato

        if (fomDato.isBefore(firstDayOfChosenYear)) {
            gjeldendeFomDato = firstDayOfChosenYear
        }
        if (tomDato == null || tomDato.isAfter(lastDayOfChosenYear)) {
            gjeldendeTomDato = lastDayOfChosenYear
        }

        return (ChronoUnit.MONTHS.between(gjeldendeFomDato, gjeldendeTomDato) + 1).toInt()
    }

    private fun validateFaktoromregnetEllerManueltOverstyrt(simuleringsresultat: SimulerEndringUforetrygdResponse): InntektsplanleggerMessage? {
        if (simuleringsresultat.isFaktoromregnetEllerManueltOverstyrt) {
            return InntektsplanleggerMessage(InntektsplanleggerMessageCode.FAKTOROMREGNET_ELLER_MANUELT_OVERSTYRT)
        }
        return null
    }

    private fun validateOpenInntektskrav(simuleringsresultat: SimulerEndringUforetrygdResponse): InntektsplanleggerMessage? {
        if (simuleringsresultat.hasOpenInntektsendringskrav) {
            return InntektsplanleggerMessage(InntektsplanleggerMessageCode.OPEN_INNTEKTSENDRING_KRAV)
        }
        return null
    }

    private fun validateMotregning(simuleringsresultat: SimulerEndringUforetrygdResponse): InntektsplanleggerMessage? {
        if (simuleringsresultat.containsMotregning) {
            return InntektsplanleggerMessage(InntektsplanleggerMessageCode.SIMULERING_CONTAINS_MOTREGNING)
        }
        return null
    }
}