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
import java.time.LocalDate
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

        val today = nowProvider.now()
        val messages = validator.validateUserInitialData(uforetrygd, aktuelleAar)

        val response: StartsideData = if (uforetrygd == null || messages.any { it.type == InntektsplanleggerMessageType.ERROR }) {
            StartsideData(
                messages = messages,
                uforetrygdIAar = null,
                uforetrygdNesteAar = null,
                aarKanRegistrereInntekt = aktuelleAar,
                seTallForAar = getAnnetRelevantAar(today),
            )
        } else {
            StartsideData(
                messages,
                uforetrygdIAar = if (aktuelleAar.contains(today.year)) {
                    finnStartsideData(
                        pid,
                        uforetrygd,
                        today.year,
                        uforetrygd.bunnfradrag,
                        uforetrygd.reduksjonsprosent,
                        uforetrygd.inntektstak)
                } else null,
                uforetrygdNesteAar = if (aktuelleAar.contains(today.year + 1)) {
                    finnStartsideData(
                        pid,
                        uforetrygd,
                        today.year + 1,
                        uforetrygd.bunnfradragNesteAar,
                        uforetrygd.reduksjonsprosentNesteAar,
                        uforetrygd.inntektstakNesteAar
                    )
                } else null,
                seTallForAar = getAnnetRelevantAar(today),
                aarKanRegistrereInntekt = aktuelleAar,
            )
        }

        StartsideInntektsplanleggerMetricsCounter.count(response)
        return response
    }

    private fun finnStartsideData(
        pid: String,
        uføretrygd: Uforetrygd,
        år: Int,
        bunnfradrag: Int,
        reduksjonsprosent: Double,
        inntektstak: Int,
    ) : UføretrygdOversikt {
        val forventedeInntekter = inntektService.getForventedeInntekter(pid, uføretrygd, år)
        return UføretrygdOversikt(
            forAar = år,
            forventetInntekt = forventedeInntekter.sumBenyttedeInntekterBruker,
            forventetInntektAnnenForelder = forventedeInntekter.sumBenyttedeInntekterEps ?: 0,
            bunnfradrag = bunnfradrag,
            reduksjonsprosent = reduksjonsprosent,
            inntektstak = inntektstak,
            harBarnetilleggFellesbarn = uføretrygd.barnetilleggFellesbarn,
        )
    }

    internal fun getAnnetRelevantAar(dato: LocalDate): Int? =
        if (dato.month.value == Month.DECEMBER.value) dato.year else null
}