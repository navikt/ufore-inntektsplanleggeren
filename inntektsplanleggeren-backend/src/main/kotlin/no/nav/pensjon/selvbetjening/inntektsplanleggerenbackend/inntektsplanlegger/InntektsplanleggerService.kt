package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.dto.InntektsplanleggerMessage
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.dto.InntektsplanleggerMessageCode
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.dto.InntektsplanleggerenInitialData
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.dto.InntektsplanleggerenInitialResponse
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.PenClient
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.Month

@Service
class InntektsplanleggerService(val penClient: PenClient) {

    fun constructInitialInntektsplanleggerResponse(pid: String): InntektsplanleggerenInitialResponse {
        val initalPensjonsdata = penClient.fetchInitialInntektsplanleggerPensjonsdata(pid)
            ?: return InntektsplanleggerenInitialResponse(
                listOf(
                    InntektsplanleggerMessage(InntektsplanleggerMessageCode.USER_HAS_NO_UFORE)
                ), null
            )
        return InntektsplanleggerenInitialResponse(
            emptyList(), InntektsplanleggerenInitialData(
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
        )
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