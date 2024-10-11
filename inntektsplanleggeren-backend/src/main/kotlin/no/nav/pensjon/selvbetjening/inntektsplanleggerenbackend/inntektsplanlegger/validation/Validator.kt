package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.InntektService
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.dto.Inntektshendelse
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.ForventedeInntekterSummary
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.InntekterHittilIAar
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.Maanedsinntekt
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.inntekt.ForventedeInntekter
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.Pensjonsdata
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.SimulerEndringUforetrygdResponse
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.person.PersonService
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.Month
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.stream.Collectors

@Service
class Validator(private val inntektService: InntektService, val personService: PersonService) {
    fun validateUserInitialData(pensjonsdata: Pensjonsdata?): List<InntektsplanleggerMessage> {
        if (pensjonsdata == null) {
            return listOf(InntektsplanleggerMessage(InntektsplanleggerMessageCode.USER_HAS_NO_UFORE))
        }

        if (!pensjonsdata.hasLopendeUforeVedtakNextYear && !pensjonsdata.hasLopendeUforeVedtakThisYear) {
            return listOf(InntektsplanleggerMessage(InntektsplanleggerMessageCode.USER_HAS_NO_LOPENDE_VEDTAK_YET))
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
                validateInntekter(
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
        if (simuleringsresultat.sumNettoRestArWithoutBTandET > maksimaltGjenstaendeIAr) {
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

    private fun validateMonth(simuleringsaar: Int): InntektsplanleggerMessage? {
        val today = LocalDate.now()
        if (today.year == simuleringsaar && today.month == Month.DECEMBER) {
            return InntektsplanleggerMessage(InntektsplanleggerMessageCode.ILLEGAL_MONTH_DECEMBER_THIS_YEAR)
        }
        return null
    }

    private fun validateInntekter(
        pid: String,
        oppgitteForventedeInntekter: ForventedeInntekter,
        pensjonsdata: Pensjonsdata,
        registrerteForventedeInntekter: ForventedeInntekterSummary?,
        simuleringsaar: Int
    ): List<InntektsplanleggerMessage> {
        val messages = mutableListOf<InntektsplanleggerMessage>()

        messages.addAll(validateInntektValidity(oppgitteForventedeInntekter))
        messages.addAll(
            validateInntektAndBarnetillegg(
                pensjonsdata.barnetilleggFellesbarn,
                pensjonsdata.barnetilleggSaerkullsbarn,
                oppgitteForventedeInntekter
            )
        )

        val inntekterHittilIAar = inntektService.getInntekterHittilIAar(
            pid,
            pensjonsdata,
            simuleringsaar
        )

        messages.addAll(validateInntektInputMatchingInntektHittilIAar(oppgitteForventedeInntekter, inntekterHittilIAar))

        val forventedeInntekter = registrerteForventedeInntekter?.mostRecentForventedeInntekterRegistrertAndBenyttet
            ?: inntektService.getForventedeInntekter(
                pid,
                pensjonsdata,
                simuleringsaar
            ).mostRecentForventedeInntekterRegistrertAndBenyttet

        validateInntektStatus(forventedeInntekter)?.let { messages.add(it) }
        validateEpsInntektChanged(oppgitteForventedeInntekter, forventedeInntekter)?.let { messages.add(it) }
        return messages
    }

    private fun validateInntektAndBarnetillegg(
        barnetilleggFellesbarn: Boolean,
        barnetilleggSaerkullsbarn: Boolean,
        inputInntektData: ForventedeInntekter
    ): List<InntektsplanleggerMessage> {
        val messages = mutableListOf<InntektsplanleggerMessage>()

        val hasBarnetillegg = barnetilleggFellesbarn || barnetilleggSaerkullsbarn

        if (!hasBarnetillegg && (inputInntektData.bruker.andrePensjonsgivendeYtelser != null || inputInntektData.bruker.pensjonUtland != null)) {
            messages.add(InntektsplanleggerMessage(InntektsplanleggerMessageCode.INNTEKT_ONLY_RELEVANT_WHEN_BARNETILLEGG))
        } else if (hasBarnetillegg && (inputInntektData.bruker.andrePensjonsgivendeYtelser == null || inputInntektData.bruker.pensjonUtland == null)) {
            messages.add(InntektsplanleggerMessage(InntektsplanleggerMessageCode.MISSING_RELEVANT_INNTEKTER_WHEN_BARNETILLEGG))
        }

        if (!barnetilleggFellesbarn && inputInntektData.eps != null) {
            messages.add(InntektsplanleggerMessage(InntektsplanleggerMessageCode.EPS_INNTEKT_ONLY_RELEVANT_WHEN_BARNETILLEGG_FELLESBARN))
        } else if (barnetilleggFellesbarn &&
            (inputInntektData.eps?.arbeidsinntekt == null
                    || inputInntektData.eps.naeringsinntekt == null
                    || inputInntektData.eps.inntektUtland == null
                    || inputInntektData.eps.pensjonUtland == null
                    || inputInntektData.eps.andrePensjonsgivendeYtelser == null)
        ) {
            messages.add(InntektsplanleggerMessage(InntektsplanleggerMessageCode.MISSING_RELEVANT_EPS_INNTEKT_WHEN_BARNETILLEGG_FELLESBARN))
        }
        return messages
    }

    private fun validateInntektValidity(inputInntektData: ForventedeInntekter): List<InntektsplanleggerMessage> {
        val messages = mutableListOf<InntektsplanleggerMessage>()
        validateInntektFieldValueValidity(
            inputInntektData.bruker.arbeidsinntekt,
            FieldReference.ARBEIDSINNTEKT_BRUKER,
            false
        )?.let { messages.add(it) }
        validateInntektFieldValueValidity(
            inputInntektData.bruker.naeringsinntekt,
            FieldReference.NAERINGSINNTEKT_BRUKER,
            false
        )?.let { messages.add(it) }
        validateInntektFieldValueValidity(
            inputInntektData.bruker.inntektUtland,
            FieldReference.INNTEKT_UTLAND_BRUKER,
            false
        )?.let { messages.add(it) }
        validateInntektFieldValueValidity(
            inputInntektData.bruker.pensjonUtland,
            FieldReference.PENSJON_UTLAND_BRUKER,
            true
        )?.let { messages.add(it) }
        validateInntektFieldValueValidity(
            inputInntektData.bruker.andrePensjonsgivendeYtelser,
            FieldReference.ANDRE_YTELSER_BRUKER,
            true
        )?.let { messages.add(it) }
        validateInntektFieldValueValidity(
            inputInntektData.eps?.arbeidsinntekt,
            FieldReference.ARBEIDSINNTEKT_EPS,
            true
        )?.let { messages.add(it) }
        validateInntektFieldValueValidity(
            inputInntektData.eps?.naeringsinntekt,
            FieldReference.NAERINGSINNTEKT_EPS,
            true
        )?.let { messages.add(it) }
        validateInntektFieldValueValidity(
            inputInntektData.eps?.inntektUtland,
            FieldReference.INNTEKT_UTLAND_EPS,
            true
        )?.let { messages.add(it) }
        validateInntektFieldValueValidity(
            inputInntektData.eps?.pensjonUtland,
            FieldReference.PENSJON_UTLAND_EPS,
            true
        )?.let { messages.add(it) }
        validateInntektFieldValueValidity(
            inputInntektData.eps?.andrePensjonsgivendeYtelser,
            FieldReference.ANDRE_YTELSER_EPS,
            true
        )?.let { messages.add(it) }
        return messages
    }

    private fun validateInntektFieldValueValidity(
        fieldValue: Int?,
        fieldReference: FieldReference,
        canBeNull: Boolean
    ): InntektsplanleggerMessage? {
        if (fieldValue == null && !canBeNull) {
            return InntektsplanleggerMessage(
                messageCode = InntektsplanleggerMessageCode.FIELD_CAN_NOT_BE_NULL,
                metadata = mapOf(MetadataKey.AFFECTED_FIELD to fieldReference.name)
            )
        }
        if (fieldValue != null && fieldValue < 0) {
            return InntektsplanleggerMessage(
                messageCode = InntektsplanleggerMessageCode.ILLEGAL_INNTEKT_FIELD_VALUE,
                metadata = mapOf(MetadataKey.AFFECTED_FIELD to fieldReference.name)
            )
        }
        return null
    }

    private fun validateInntektInputMatchingInntektHittilIAar(
        inntektInput: ForventedeInntekter,
        inntekterHittilIAar: InntekterHittilIAar
    ): List<InntektsplanleggerMessage> {
        val messages = mutableListOf<InntektsplanleggerMessage>()

        val sumInntekterHittilIAarBruker =
            sumMaanedsinntekter(inntekterHittilIAar.arbeidsinntektOgPensjonsgivendeYtelser)
        val sumAndreYtelserHittilIAarBruker = sumMaanedsinntekter(inntekterHittilIAar.pensjonerFraAndreEnnFolketrygden)
        val sumInntekterHittilIAarEps =
            sumMaanedsinntekter(inntekterHittilIAar.arbeidsinntektOgPensjonsgivendeYtelserEps)
        val sumAndreYtelserHittilIAarEps = sumMaanedsinntekter(inntekterHittilIAar.pensjonerFraAndreEnnFolketrygdenEps)

        if ((inntektInput.bruker.arbeidsinntekt ?: 0) < sumInntekterHittilIAarBruker) {
            messages.add(
                InntektsplanleggerMessage(
                    messageCode = InntektsplanleggerMessageCode.ARBEIDSINNTEKT_GIVEN_SMALLER_THAN_HITTIL_I_AAR,
                    metadata = mapOf(
                        MetadataKey.AFFECTED_FIELD to FieldReference.ARBEIDSINNTEKT_BRUKER.name
                    )
                )
            )
        }

        if ((inntektInput.eps?.arbeidsinntekt ?: 0) < sumInntekterHittilIAarEps) {
            messages.add(
                InntektsplanleggerMessage(
                    messageCode = InntektsplanleggerMessageCode.ARBEIDSINNTEKT_GIVEN_SMALLER_THAN_HITTIL_I_AAR,
                    metadata = mapOf(
                        MetadataKey.AFFECTED_FIELD to FieldReference.ARBEIDSINNTEKT_EPS.name
                    )
                )
            )
        }

        if ((inntektInput.bruker.andrePensjonsgivendeYtelser ?: 0) < sumAndreYtelserHittilIAarBruker) {
            messages.add(
                InntektsplanleggerMessage(
                    messageCode = InntektsplanleggerMessageCode.ANDRE_YTELSER_SMALLER_THAN_HITTIL_I_AAR,
                    metadata = mapOf(
                        MetadataKey.AFFECTED_FIELD to FieldReference.ANDRE_YTELSER_BRUKER.name
                    )
                )
            )
        }

        if ((inntektInput.eps?.andrePensjonsgivendeYtelser ?: 0) < sumAndreYtelserHittilIAarEps) {
            messages.add(
                InntektsplanleggerMessage(
                    messageCode = InntektsplanleggerMessageCode.ANDRE_YTELSER_SMALLER_THAN_HITTIL_I_AAR,
                    metadata = mapOf(
                        MetadataKey.AFFECTED_FIELD to FieldReference.ANDRE_YTELSER_EPS.name
                    )
                )
            )
        }
        return messages
    }

    private fun validateInntektStatus(forventedeInntekter: no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.ForventedeInntekter): InntektsplanleggerMessage? {
        if (forventedeInntekter.bruker.arbeidsinntekt?.status == Inntektshendelse.REGISTRERT
            || forventedeInntekter.bruker.naeringsinntekt?.status == Inntektshendelse.REGISTRERT
            || forventedeInntekter.bruker.inntektUtland?.status == Inntektshendelse.REGISTRERT
            || forventedeInntekter.eps?.arbeidsinntekt?.status == Inntektshendelse.REGISTRERT
            || forventedeInntekter.eps?.inntektUtland?.status == Inntektshendelse.REGISTRERT
            || forventedeInntekter.eps?.naeringsinntekt?.status == Inntektshendelse.REGISTRERT
        ) {
            return InntektsplanleggerMessage(InntektsplanleggerMessageCode.ONE_OR_MORE_INNTEKT_HAS_STATUS_REGISTRERT)
        }
        return null
    }

    private fun validateEpsInntektChanged(
        inputInntektData: ForventedeInntekter,
        registeredForventedeInntekter: no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.ForventedeInntekter
    ): InntektsplanleggerMessage? {
        if (inputInntektData.eps?.arbeidsinntekt != registeredForventedeInntekter.eps?.arbeidsinntekt?.belop
            || inputInntektData.eps?.naeringsinntekt != registeredForventedeInntekter.eps?.naeringsinntekt?.belop
            || inputInntektData.eps?.inntektUtland != registeredForventedeInntekter.eps?.inntektUtland?.belop
            || inputInntektData.eps?.andrePensjonsgivendeYtelser != registeredForventedeInntekter.eps?.andrePensjonsgivendeYtelser?.belop
            || inputInntektData.eps?.pensjonUtland != registeredForventedeInntekter.eps?.pensjonUtland?.belop
        ) {
            return InntektsplanleggerMessage(InntektsplanleggerMessageCode.EPS_INNTEKT_CHANGED)
        }
        return null
    }

    private fun sumMaanedsinntekter(inntekter: List<Maanedsinntekt>?): Double {
        return inntekter?.stream()
            ?.map { it.belop }
            ?.collect(Collectors.toList())
            ?.sum() ?: 0.0
    }
}