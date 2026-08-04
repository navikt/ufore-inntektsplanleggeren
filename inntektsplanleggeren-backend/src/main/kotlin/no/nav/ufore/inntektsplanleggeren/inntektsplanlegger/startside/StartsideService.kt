package no.nav.ufore.inntektsplanleggeren.inntektsplanlegger.startside


import no.nav.ufore.inntektsplanleggeren.metrics.StartsideInntektsplanleggerMetricsCounter
import no.nav.ufore.inntektsplanleggeren.inntekt.InntektService
import no.nav.ufore.inntektsplanleggeren.inntektsplanlegger.InntektsplanleggerService
import no.nav.ufore.inntektsplanleggeren.inntektsplanlegger.validation.InntektsplanleggerMessage
import no.nav.ufore.inntektsplanleggeren.inntektsplanlegger.validation.InntektsplanleggerMessageType
import no.nav.ufore.inntektsplanleggeren.inntektsplanlegger.validation.Validator
import no.nav.ufore.inntektsplanleggeren.pensjon.PenClient
import no.nav.ufore.inntektsplanleggeren.pensjon.dto.Uforetrygd
import no.nav.ufore.inntektsplanleggeren.util.NowProvider
import org.springframework.stereotype.Service
import java.time.Month

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

    internal fun getAnnetRelevantAar(): Int? =
        if (nowProvider.now().month.value == Month.DECEMBER.value) {
            nowProvider.now().year
        } else null

    private fun getAktuelleAarForInntekt(aktuelleAar: List<Int>): List<Int> =
        if (nowProvider.now().month.value == Month.DECEMBER.value) {
            (listOf(nowProvider.now().year) + aktuelleAar).distinct()
        } else {
            aktuelleAar
        }


}