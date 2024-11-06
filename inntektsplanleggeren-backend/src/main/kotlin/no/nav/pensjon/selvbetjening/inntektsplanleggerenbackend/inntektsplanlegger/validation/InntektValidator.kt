package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.InntektService
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.dto.Inntektshendelse
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.ForventedeInntekterSummary
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.InntekterHittilIAar
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.Maanedsinntekt
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.inntekt.ForventedeInntekter
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.Pensjonsdata
import org.springframework.stereotype.Service
import java.util.stream.Collectors

@Service
class InntektValidator(val inntektService: InntektService) {
    fun validateInntekter(
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

        if (inntektInput.bruker.arbeidsinntekt != null && inntektInput.bruker.arbeidsinntekt < sumInntekterHittilIAarBruker) {
            messages.add(
                InntektsplanleggerMessage(
                    messageCode = InntektsplanleggerMessageCode.ARBEIDSINNTEKT_GIVEN_SMALLER_THAN_HITTIL_I_AAR,
                    metadata = mapOf(
                        MetadataKey.AFFECTED_FIELD to FieldReference.ARBEIDSINNTEKT_BRUKER.name,
                        MetadataKey.SUM_HITTIL_I_AAR to sumInntekterHittilIAarBruker
                    )
                )
            )
        }

        if (inntektInput.eps?.arbeidsinntekt != null && inntektInput.eps.arbeidsinntekt < sumInntekterHittilIAarEps) {
            messages.add(
                InntektsplanleggerMessage(
                    messageCode = InntektsplanleggerMessageCode.ARBEIDSINNTEKT_GIVEN_SMALLER_THAN_HITTIL_I_AAR,
                    metadata = mapOf(
                        MetadataKey.AFFECTED_FIELD to FieldReference.ARBEIDSINNTEKT_EPS.name
                    )
                )
            )
        }

        if (inntektInput.bruker.andrePensjonsgivendeYtelser != null && inntektInput.bruker.andrePensjonsgivendeYtelser < sumAndreYtelserHittilIAarBruker) {
            messages.add(
                InntektsplanleggerMessage(
                    messageCode = InntektsplanleggerMessageCode.ANDRE_YTELSER_SMALLER_THAN_HITTIL_I_AAR,
                    metadata = mapOf(
                        MetadataKey.AFFECTED_FIELD to FieldReference.ANDRE_YTELSER_BRUKER.name,
                        MetadataKey.SUM_HITTIL_I_AAR to sumAndreYtelserHittilIAarBruker
                    )
                )
            )
        }

        if (inntektInput.eps?.andrePensjonsgivendeYtelser != null && inntektInput.eps.andrePensjonsgivendeYtelser < sumAndreYtelserHittilIAarEps) {
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