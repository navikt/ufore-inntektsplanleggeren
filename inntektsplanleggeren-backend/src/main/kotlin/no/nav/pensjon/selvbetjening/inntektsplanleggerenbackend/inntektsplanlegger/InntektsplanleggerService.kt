package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation.InntektsplanleggerMessage
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation.InntektsplanleggerMessageCode
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.dto.InntektsplanleggerenInitialData
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.dto.InntektsplanleggerenInitialResponse
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation.Validator
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.PenClient
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.InitialInntektsplanleggerPensjonsdata
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.Month

@Service
class InntektsplanleggerService(
    val penClient: PenClient,
    val validator: Validator
) {

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
}