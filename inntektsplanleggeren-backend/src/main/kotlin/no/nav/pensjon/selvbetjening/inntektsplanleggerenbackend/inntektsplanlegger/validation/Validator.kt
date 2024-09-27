package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.InntektService
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.dto.Inntektshendelse
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.InntekterHittilIAar
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.Maanedsinntekt
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.dto.ForventedeInntekter
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.InitialInntektsplanleggerPensjonsdata
import org.springframework.stereotype.Service
import java.util.stream.Collectors

@Service
class Validator(val inntektService: InntektService) {
    fun validateUserInitialData(initialPensjonsdata: InitialInntektsplanleggerPensjonsdata?): List<InntektsplanleggerMessage> {
        if (initialPensjonsdata == null) {
            return listOf(InntektsplanleggerMessage(InntektsplanleggerMessageCode.USER_HAS_NO_UFORE))
        }

        if (!initialPensjonsdata.hasLopendeUforeVedtakNextYear && !initialPensjonsdata.hasLopendeUforeVedtakThisYear) {
            return listOf(InntektsplanleggerMessage(InntektsplanleggerMessageCode.USER_HAS_NO_LOPENDE_VEDTAK_YET))
        }
        return emptyList()
    }

    fun validateUserAndInputBeforeSimulering(
        initialPensjonsdata: InitialInntektsplanleggerPensjonsdata?,
        inputInntektData: ForventedeInntekter,
        pid: String,
        epsPid: String?,
        barnetilleggFellesbarn: Boolean,
        barnetilleggSaerkullsbarn: Boolean,
        simuleringsaar: Int
    ): List<InntektsplanleggerMessage> {
        val validationMessages = mutableListOf<InntektsplanleggerMessage>()
        validationMessages.addAll(validateUserInitialData(initialPensjonsdata))
        validationMessages.addAll(
            validateInntekter(
                pid,
                inputInntektData,
                epsPid,
                barnetilleggFellesbarn,
                barnetilleggSaerkullsbarn,
                simuleringsaar
            )
        )

        return validationMessages
    }

    private fun validateInntekter(
        pid: String,
        inputInntektData: ForventedeInntekter,
        epsPid: String?,
        barnetilleggFellesbarn: Boolean,
        barnetilleggSaerkullsbarn: Boolean,
        simuleringsaar: Int
    ): List<InntektsplanleggerMessage> {
        val messages = mutableListOf<InntektsplanleggerMessage>()

        messages.addAll(validateInntektValidity(inputInntektData))

        val inntekterHittilIAar = inntektService.getInntekterHittilIAar(
            pid,
            epsPid,
            barnetilleggFellesbarn,
            barnetilleggSaerkullsbarn,
            simuleringsaar
        )

        messages.addAll(validateInntektInputMatchingInntektHittilIAar(inputInntektData, inntekterHittilIAar))

        val forventedeInntekter = inntektService.getForventedeInntekter(
            pid,
            epsPid,
            barnetilleggFellesbarn,
            barnetilleggSaerkullsbarn,
            simuleringsaar
        )

        validateInntektStatus(forventedeInntekter)?.let { messages.add(it) }
        validateEpsInntektChanged(inputInntektData, forventedeInntekter)?.let { messages.add(it) }

        return messages
    }

    private fun validateInntektValidity(inputInntektData: ForventedeInntekter): List<InntektsplanleggerMessage> {
        val messages = mutableListOf<InntektsplanleggerMessage>()
        validateInntektFieldValueValidity(
            inputInntektData.bruker.arbeidsinntekt,
            FieldReference.ARBEIDSINNTEKT_BRUKER
        )?.let { messages.add(it) }
        validateInntektFieldValueValidity(
            inputInntektData.bruker.naeringsinntekt,
            FieldReference.NAERINGSINNTEKT_BRUKER
        )?.let { messages.add(it) }
        validateInntektFieldValueValidity(
            inputInntektData.bruker.inntektUtland,
            FieldReference.INNTEKT_UTLAND_BRUKER
        )?.let { messages.add(it) }
        validateInntektFieldValueValidity(
            inputInntektData.bruker.pensjonUtland,
            FieldReference.PENSJON_UTLAND_BRUKER
        )?.let { messages.add(it) }
        validateInntektFieldValueValidity(
            inputInntektData.bruker.andrePensjonsgivendeYtelser,
            FieldReference.ANDRE_YTELSER_BRUKER
        )?.let { messages.add(it) }
        validateInntektFieldValueValidity(
            inputInntektData.eps?.arbeidsinntekt,
            FieldReference.ARBEIDSINNTEKT_EPS
        )?.let { messages.add(it) }
        validateInntektFieldValueValidity(
            inputInntektData.eps?.naeringsinntekt,
            FieldReference.NAERINGSINNTEKT_EPS
        )?.let { messages.add(it) }
        validateInntektFieldValueValidity(
            inputInntektData.eps?.inntektUtland,
            FieldReference.INNTEKT_UTLAND_EPS
        )?.let { messages.add(it) }
        validateInntektFieldValueValidity(
            inputInntektData.eps?.pensjonUtland,
            FieldReference.PENSJON_UTLAND_EPS
        )?.let { messages.add(it) }
        validateInntektFieldValueValidity(
            inputInntektData.eps?.andrePensjonsgivendeYtelser,
            FieldReference.ANDRE_YTELSER_EPS
        )?.let { messages.add(it) }
        return messages
    }

    private fun validateInntektFieldValueValidity(
        fieldValue: Int?,
        fieldReference: FieldReference
    ): InntektsplanleggerMessage? {
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