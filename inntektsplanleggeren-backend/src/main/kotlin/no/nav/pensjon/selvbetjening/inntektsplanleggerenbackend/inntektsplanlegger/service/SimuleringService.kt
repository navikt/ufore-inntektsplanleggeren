package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.service

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.ForventedeInntekterSummary
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.dto.BeforeAndAfterValues
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.dto.ForventedeInntekter
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.dto.Simuleringsresultat
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.PenClient
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.SimulerEndringUforetrygdResponse
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class SimuleringService(val penClient: PenClient) {
    fun simulerInntektsendring(
        pid: String,
        forventedeInntekterOppgitt: ForventedeInntekter,
        forventedeInntekter: ForventedeInntekterSummary,
        simuleringsaar: Int,
        simuleringFomDato: LocalDate
    ): Simuleringsresultat {
        val simuleringsresultat = penClient.simulerInntektsendring(
            pid = pid,
            virk = simuleringFomDato,
            inntektsgrunnlagListe = forventedeInntekterOppgitt.bruker.mapToInntektsgrunnlag(simuleringsaar),
            inntektsgrunnlagListeEps = forventedeInntekterOppgitt.eps?.mapToInntektsgrunnlag(simuleringsaar)?: emptyList()
        )
        return Simuleringsresultat(
            uforetrygd = BeforeAndAfterValues(
                before = simuleringsresultat.currentUforetrygdSummary.uforetrygdYtelseskomponenter.uforetrygdOrdiner.amountPerYear,
                after = simuleringsresultat.simulertUforetrygdSummary.uforetrygdYtelseskomponenter.uforetrygdOrdiner.amountPerYear
            ),
            forventetInntekt = BeforeAndAfterValues(
                before = forventedeInntekter.sumBenyttedeInntekterBruker,
                after = forventedeInntekterOppgitt.bruker.sum()
            ),
            barnetilleggFellesbarn = BeforeAndAfterValues(
                before = simuleringsresultat.currentUforetrygdSummary.uforetrygdYtelseskomponenter.barnetilleggFellesbarn?.amountPerYear
                    ?: 0,
                after = simuleringsresultat.simulertUforetrygdSummary.uforetrygdYtelseskomponenter.barnetilleggFellesbarn?.amountPerYear
                    ?: 0
            ),
            barnetilleggSaerkullsbarn = BeforeAndAfterValues(
                before = simuleringsresultat.currentUforetrygdSummary.uforetrygdYtelseskomponenter.barnetilleggSaerkullsbarn?.amountPerYear
                    ?: 0,
                after = simuleringsresultat.simulertUforetrygdSummary.uforetrygdYtelseskomponenter.barnetilleggSaerkullsbarn?.amountPerYear
                    ?: 0
            ),
            gjenlevendetillegg = BeforeAndAfterValues(
                before = simuleringsresultat.currentUforetrygdSummary.uforetrygdYtelseskomponenter.gjenlevendetillegg?.amountPerYear
                    ?: 0,
                after = simuleringsresultat.simulertUforetrygdSummary.uforetrygdYtelseskomponenter.gjenlevendetillegg?.amountPerYear
                    ?: 0
            ),
            sum = BeforeAndAfterValues(
                before = getSumArligUforetrygd(simuleringsresultat, simuleringsaar),
                after = getSumArligUforetrygdSimulert(simuleringsresultat)
            ),
            uforetrygdPerMaaned = 0
        )
    }

    private fun getSumArligUforetrygd(simuleringsresultat: SimulerEndringUforetrygdResponse, simuleringsaar: Int): Int {
        return if (simuleringsresultat.gjeldendeBeregningFom?.year == simuleringsaar) {
            simuleringsresultat.currentUforetrygdSummary.sumYtelseskomponenter ?: 0
        } else {
            simuleringsresultat.currentUforetrygdSummary.totalbelopNetto ?: 0
        }
    }

    private fun getSumArligUforetrygdSimulert(simuleringsresultat: SimulerEndringUforetrygdResponse): Int {
        return simuleringsresultat.simulertUforetrygdSummary.sumYtelseskomponenter ?: 0
    }
}