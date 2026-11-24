package no.nav.ufore.inntektsplanleggeren.inntektsplanlegger.startside


import no.nav.ufore.inntektsplanleggeren.metrics.StartsideInntektsplanleggerMetricsCounter
import no.nav.ufore.inntektsplanleggeren.inntekt.InntektService
import no.nav.ufore.inntektsplanleggeren.inntektsplanlegger.InntektsplanleggerService
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

        val dataMap: MutableMap<Int, UføretrygdOversikt> = mutableMapOf()

        if (uforetrygd == null || messages.any { it.type == InntektsplanleggerMessageType.ERROR }) {
            return StartsideData(
                messages = messages,
                uforetrygdIAar = null,
                uforetrygdNesteAar = null,
                aarKanRegistrereInntekt = aktuelleAar,
                seTallForAar = getAnnetRelevantAar(),
            )
        }

        val today = NowProvider().now()
        if (aktuelleAar.contains(today.year)) {
            dataMap[today.year] = finnStartsideDataForIÅr(pid,uforetrygd, today.year)
        }

        if (aktuelleAar.contains(today.year + 1)) {
            dataMap[today.year + 1] = finnStartsideDataForNesteÅr(pid,uforetrygd, today.year + 1)
        }

        val response = StartsideData(
            messages,
            uforetrygdIAar = dataMap[today.year],
            uforetrygdNesteAar = dataMap[today.year + 1],
            seTallForAar = getAnnetRelevantAar(),
            aarKanRegistrereInntekt = aktuelleAar,
        )

        StartsideInntektsplanleggerMetricsCounter.count(response)

        return response
    }

    private fun finnStartsideDataForIÅr(pid: String, uføretrygd: Uforetrygd, år: Int): UføretrygdOversikt {
        val forventedeInntekter = inntektService.getForventedeInntekter(pid, uføretrygd, år)
        return UføretrygdOversikt(
            forAar = år,
            forventetInntekt = forventedeInntekter.sumBenyttedeInntekterBruker,
            forventetInntektAnnenForelder = forventedeInntekter.sumBenyttedeInntekterEps ?: 0,
            bunnfradrag = uføretrygd.bunnfradrag,
            reduksjonsprosent = uføretrygd.reduksjonsprosent,
            inntektstak = uføretrygd.inntektstak,
            harBarnetilleggFellesbarn = uføretrygd.barnetilleggFellesbarn,
        )
    }

    private fun finnStartsideDataForNesteÅr(pid: String, uføretrygd: Uforetrygd, år: Int): UføretrygdOversikt {
        val forventedeInntekter = inntektService.getForventedeInntekter(pid, uføretrygd, år)
        return UføretrygdOversikt(
            forAar = år,
            forventetInntekt = forventedeInntekter.sumBenyttedeInntekterBruker,
            forventetInntektAnnenForelder = forventedeInntekter.sumBenyttedeInntekterEps ?: 0,
            bunnfradrag = uføretrygd.bunnfradragNesteAar,
            reduksjonsprosent = uføretrygd.reduksjonsprosentNesteAar,
            inntektstak = uføretrygd.inntektstakNesteAar,
            harBarnetilleggFellesbarn = uføretrygd.barnetilleggFellesbarn,
        )
    }

    internal fun getAnnetRelevantAar(): Int? =
        if (nowProvider.now().month.value == Month.DECEMBER.value) {
            nowProvider.now().year
        } else null

}