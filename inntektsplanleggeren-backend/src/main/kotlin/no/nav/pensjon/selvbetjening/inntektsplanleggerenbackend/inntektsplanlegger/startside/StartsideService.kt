package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.startside


import no.nav.pensjon.selvbetjening.alderspensjonendringssoknadbackend.metrics.StartsideInntektsplanleggerMetricsCounter
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.InntektService
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.InntektsplanleggerService
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.StartsideData
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.UføretrygdOversikt
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation.InntektsplanleggerMessage
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation.InntektsplanleggerMessageType
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation.Validator
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.PenClient
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.Uforetrygd
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.util.NowProvider
import org.springframework.stereotype.Service

@Service
class StartsideService(
    private val penClient: PenClient,
    private val validator: Validator,
    private val inntektService: InntektService,
    private val inntektsplanleggerService: InntektsplanleggerService,
    private val nowProvider: NowProvider,
) {

    fun hentStartsideData(
        pid: String,
        simuleringsaar: Int
    ): StartsideData {
        val uforetrygd = penClient.fetchInntektsplanleggerData(pid, inntektsplanleggerService.getSimuleringFomDato(simuleringsaar))
        val aktuelleAar = uforetrygd.let {
            inntektsplanleggerService.finnAarKanRegistrereFor(
                it?.hasLopendeUforeVedtakThisYear,
                it?.hasLopendeUforeVedtakNextYear
            )
        }
        val messages = validator.validateUserInitialData(uforetrygd, aktuelleAar)

        val response = StartsideData(
            messages,
            mapInntektsplanleggerenInitialData(pid, uforetrygd, aktuelleAar, messages),
        )

        StartsideInntektsplanleggerMetricsCounter.count(response)

        return response
    }

    private fun mapInntektsplanleggerenInitialData(
        pid: String,
        uforetrygd: Uforetrygd?,
        aktuelleAar: List<Int>,
        messages: List<InntektsplanleggerMessage>
    ): UføretrygdOversikt? {
        if (uforetrygd != null && messages.none { it.type == InntektsplanleggerMessageType.ERROR }) {
            val forventedeInntekter = getAktuelleAarForInntekt(aktuelleAar).associateWith { inntektService.getForventedeInntekter(pid, uforetrygd, it) }

            return UføretrygdOversikt(
                forventetInntekt = forventedeInntekter.map { it.key to it.value.sumBenyttedeInntekterBruker }.toMap(),
                forventetInntektAnnenForelder = forventedeInntekter.map { it.key to it.value.sumBenyttedeInntekterEps }.toMap(),
                inntektsgrense = uforetrygd.inntektsgrense,
                reduksjonsprosent = uforetrygd.kompensasjonsgrad,
                inntektstak = uforetrygd.grenseStoppAvUfoeretrygd,
                harBarnetilleggFellesbarn = uforetrygd.barnetilleggFellesbarn,
                aarKanRegistrereInntekt = aktuelleAar,
                seTallForAar = getAnnetRelevantAar()
            )
        }
        return null
    }

    private fun getAnnetRelevantAar(): Int? =
        if (inntektsplanleggerService.isMonthDecember()) {
            nowProvider.now().year
        } else null

    private fun getAktuelleAarForInntekt(aktuelleAar: List<Int>): List<Int> =
        if (inntektsplanleggerService.isMonthDecember()) {
            (listOf(nowProvider.now().year) + aktuelleAar).distinct()
        } else {
            aktuelleAar
        }


}