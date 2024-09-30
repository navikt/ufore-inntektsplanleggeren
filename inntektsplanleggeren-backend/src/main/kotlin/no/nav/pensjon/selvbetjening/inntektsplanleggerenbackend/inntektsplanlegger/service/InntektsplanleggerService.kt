package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.service

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.Maanedsinntekt
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.InntektService
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.dto.*
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation.InntektsplanleggerMessage
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation.Validator
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.PenClient
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.InitialInntektsplanleggerPensjonsdata
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.Month

@Service
class InntektsplanleggerService(
    val penClient: PenClient,
    val validator: Validator,
    val inntektService: InntektService
) {

    fun simulerInntektsendring(pid: String, simuleringsAar: Int, inntekter: ForventedeInntekter): SimuleringResponse {
        val initalPensjonsdata = penClient.fetchInitialInntektsplanleggerPensjonsdata(pid) //TODO: Vurder å effektivisere ved at disse to PEN-kallene slås sammen, kanskje?
        val inntektGrunnlagsdata = penClient.fetchGrunnlagForInntekter(pid)
        val validationResult = validator.validateUserAndInputBeforeSimulering(
            initalPensjonsdata,
            inntekter,
            pid,
            inntektGrunnlagsdata.epsPid,
            inntektGrunnlagsdata.barnetilleggFellesbarn,
            inntektGrunnlagsdata.barnetilleggSaerkullsbarn,
            simuleringsAar
        )

        //TODO: Kall simuleringstjeneste i PEN og inkluder resultatet i responsen. (ikke kall simulering i PEN hvis valideringen returnerer ERROR)

        return SimuleringResponse(validationResult, null)
    }

    fun constructInntekterResponse(pid: String, simuleringsaar: Int): InntekterResponse {
        val inntektGrunnlagsdata = penClient.fetchGrunnlagForInntekter(pid)
        val inntekterHittilIAar = inntektService.getInntekterHittilIAar(
            pid,
            inntektGrunnlagsdata.epsPid,
            inntektGrunnlagsdata.barnetilleggFellesbarn,
            inntektGrunnlagsdata.barnetilleggSaerkullsbarn,
            simuleringsaar
        )

        return InntekterResponse(
            arbeidsinntektOgYtelserHittilIAar = accumulateAllInntekterForSameMonth(inntekterHittilIAar.arbeidsinntektOgPensjonsgivendeYtelser),
            pensjonFraAndreHittilIAar = accumulateAllInntekterForSameMonth(inntekterHittilIAar.pensjonerFraAndreEnnFolketrygden),
            forventedeInntekter = inntektService.getForventedeInntekter(
                pid,
                inntektGrunnlagsdata.epsPid,
                inntektGrunnlagsdata.barnetilleggFellesbarn,
                inntektGrunnlagsdata.barnetilleggSaerkullsbarn,
                simuleringsaar
            ).toDto(),
            uforeHeleAaret = inntektGrunnlagsdata.uforeHeleAaret
        )
    }

    fun constructInitialInntektsplanleggerResponse(pid: String): InntektsplanleggerenInitialResponse {
        val initalPensjonsdata = penClient.fetchInitialInntektsplanleggerPensjonsdata(pid)
        val messages = validator.validateUserInitialData(initalPensjonsdata)
        return InntektsplanleggerenInitialResponse(
            messages,
            mapInntektsplanleggerenInitialData(initalPensjonsdata, messages)
        )
    }

    private fun mapInntektsplanleggerenInitialData(
        initalPensjonsdata: InitialInntektsplanleggerPensjonsdata?,
        messages: List<InntektsplanleggerMessage>
    ): InntektsplanleggerenInitialData? {
        if (initalPensjonsdata != null && messages.isEmpty()) {
            return InntektsplanleggerenInitialData(
                forventetInntekt = initalPensjonsdata.forventetInntekt,
                forventetInntektAnnenForelder = initalPensjonsdata.forventetInntektAnnenForelder,
                inntektsgrense = initalPensjonsdata.inntektsgrense,
                kompensasjonsgrad = initalPensjonsdata.kompensasjonsgrad,
                grenseStoppAvUfoeretrygd = initalPensjonsdata.grenseStoppAvUfoeretrygd,
                hasGjenlevendeTillegg = initalPensjonsdata.hasGjenlevendeTillegg,
                hasBarneTillegg = initalPensjonsdata.hasBarneTillegg,
                hasVarigTilrettelagtArbeid = initalPensjonsdata.hasVarigTilrettelagtArbeid,
                aktuelleAar = getAktuelleAar(
                    initalPensjonsdata.hasLopendeUforeVedtakThisYear,
                    initalPensjonsdata.hasLopendeUforeVedtakNextYear
                )
            )
        }
        return null
    }

    private fun getAktuelleAar(
        hasLopendeUforeVedtakThisYear: Boolean,
        hasLopendeUforeVedtakNextYear: Boolean
    ): List<Int> {
        val today = LocalDate.now()
        val isMonthBeforeOctober = today.month.value < Month.OCTOBER.value

        if (isMonthBeforeOctober && hasLopendeUforeVedtakThisYear) {
            return listOf(today.year)
        }
        if (!isMonthBeforeOctober && hasLopendeUforeVedtakThisYear) {
            return listOf(today.year, today.year + 1)
        }
        if (!isMonthBeforeOctober && hasLopendeUforeVedtakNextYear) {
            return listOf(today.year + 1)
        }
        return emptyList()
    }

    private fun accumulateAllInntekterForSameMonth(maanedsinntekter: List<Maanedsinntekt>): List<AccumulatedMaanedsinntekt> {
        val inntekterEachMonth = mutableMapOf<Int, MutableList<Maanedsinntekt>>()

        maanedsinntekter.forEach { maanedsinntekt ->
            run {
                if (inntekterEachMonth.containsKey(maanedsinntekt.maned)) {
                    inntekterEachMonth[maanedsinntekt.maned]?.add(maanedsinntekt)
                } else {
                    inntekterEachMonth[maanedsinntekt.maned] = mutableListOf(maanedsinntekt)
                }
            }
        }

        return inntekterEachMonth.keys.map { maaned ->
            AccumulatedMaanedsinntekt(
                maaned,
                inntekterEachMonth[maaned]?.sumOf { it.belop } ?: 0.0,
                inntekterEachMonth[maaned]?.map { it.utbetaltFra } ?: emptyList()
            )
        }
    }
}