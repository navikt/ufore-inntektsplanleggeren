package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.simulering

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.ForventedeInntekterSummary
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.inntekt.ForventedeInntekter
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation.Validator
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.PenClient
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.SimulerEndringUforetrygdResponse
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class SimuleringService(val penClient: PenClient, val validator: Validator) {
    fun simulerInntektsendring(
        pid: String,
        forventedeInntekterOppgitt: ForventedeInntekter,
        forventedeInntekter: ForventedeInntekterSummary,
        simuleringsaar: Int,
        simuleringFomDato: LocalDate
    ): SimuleringData {
        val simuleringsresultat = penClient.simulerInntektsendring(
            pid = pid,
            virk = simuleringFomDato,
            inntektsgrunnlagListe = forventedeInntekterOppgitt.bruker.mapToInntektsgrunnlag(simuleringsaar),
            inntektsgrunnlagListeEps = forventedeInntekterOppgitt.eps?.mapToInntektsgrunnlag(simuleringsaar)
                ?: emptyList()
        )
        val valideringsresultat = validator.validateSimulering(simuleringsresultat, forventedeInntekterOppgitt, simuleringFomDato, simuleringsaar, pid)
        return SimuleringData(
            valideringsresultat = valideringsresultat, simuleringsresultat = Simuleringsresultat(
                uforetrygd = SimuleringAmounts(
                    yearly = BeforeAndAfterValues(
                        before = simuleringsresultat.currentUforetrygdSummary.uforetrygdYtelseskomponenter.uforetrygdOrdiner.amountPerYear,
                        after = simuleringsresultat.simulertUforetrygdSummary.uforetrygdYtelseskomponenter.uforetrygdOrdiner.amountPerYear
                    ), monthly = BeforeAndAfterValues(
                        before = simuleringsresultat.currentUforetrygdSummary.uforetrygdYtelseskomponenter.uforetrygdOrdiner.amountPerMonth,
                        after = simuleringsresultat.simulertUforetrygdSummary.uforetrygdYtelseskomponenter.uforetrygdOrdiner.amountPerMonth
                    )
                ),
                forventetInntekt = SimuleringAmounts(
                    yearly = BeforeAndAfterValues(
                        before = forventedeInntekter.sumBenyttedeInntekterBruker,
                        after = forventedeInntekterOppgitt.bruker.sum()
                    ), monthly = BeforeAndAfterValues(
                        before = forventedeInntekter.sumBenyttedeInntekterBruker / 12,
                        after = forventedeInntekterOppgitt.bruker.sum() / 12
                    )
                ),
                barnetilleggFellesbarn = SimuleringAmounts(
                    yearly = BeforeAndAfterValues(
                        before = simuleringsresultat.currentUforetrygdSummary.uforetrygdYtelseskomponenter.barnetilleggFellesbarn?.amountPerYear
                            ?: 0,
                        after = simuleringsresultat.simulertUforetrygdSummary.uforetrygdYtelseskomponenter.barnetilleggFellesbarn?.amountPerYear
                            ?: 0
                    ), monthly = BeforeAndAfterValues(
                        before = simuleringsresultat.currentUforetrygdSummary.uforetrygdYtelseskomponenter.barnetilleggFellesbarn?.amountPerMonth
                            ?: 0,
                        after = simuleringsresultat.simulertUforetrygdSummary.uforetrygdYtelseskomponenter.barnetilleggFellesbarn?.amountPerMonth
                            ?: 0
                    )
                ),
                barnetilleggSaerkullsbarn = SimuleringAmounts(
                    yearly = BeforeAndAfterValues(
                        before = simuleringsresultat.currentUforetrygdSummary.uforetrygdYtelseskomponenter.barnetilleggSaerkullsbarn?.amountPerYear
                            ?: 0,
                        after = simuleringsresultat.simulertUforetrygdSummary.uforetrygdYtelseskomponenter.barnetilleggSaerkullsbarn?.amountPerYear
                            ?: 0
                    ), monthly = BeforeAndAfterValues(
                        before = simuleringsresultat.currentUforetrygdSummary.uforetrygdYtelseskomponenter.barnetilleggSaerkullsbarn?.amountPerMonth
                            ?: 0,
                        after = simuleringsresultat.simulertUforetrygdSummary.uforetrygdYtelseskomponenter.barnetilleggSaerkullsbarn?.amountPerMonth
                            ?: 0
                    )
                ),
                gjenlevendetillegg = SimuleringAmounts(
                    yearly = BeforeAndAfterValues(
                        before = simuleringsresultat.currentUforetrygdSummary.uforetrygdYtelseskomponenter.gjenlevendetillegg?.amountPerYear
                            ?: 0,
                        after = simuleringsresultat.simulertUforetrygdSummary.uforetrygdYtelseskomponenter.gjenlevendetillegg?.amountPerYear
                            ?: 0
                    ), monthly = BeforeAndAfterValues(
                        before = simuleringsresultat.currentUforetrygdSummary.uforetrygdYtelseskomponenter.gjenlevendetillegg?.amountPerMonth
                            ?: 0,
                        after = simuleringsresultat.simulertUforetrygdSummary.uforetrygdYtelseskomponenter.gjenlevendetillegg?.amountPerMonth
                            ?: 0
                    )
                ),
                sum = SimuleringAmounts(
                    yearly = BeforeAndAfterValues(
                        before = getSumArligUforetrygd(simuleringsresultat, simuleringsaar),
                        after = getSumArligUforetrygdSimulert(simuleringsresultat)
                    ), monthly = BeforeAndAfterValues(
                        before = simuleringsresultat.currentUforetrygdSummary.totalbelopNetto ?: 0,
                        after = simuleringsresultat.simulertUforetrygdSummary.totalbelopNetto ?: 0
                    )
                )
            )
        )
    }

    private fun getSumArligUforetrygd(simuleringsresultat: SimulerEndringUforetrygdResponse, simuleringsaar: Int): Int {
        return if (simuleringsresultat.gjeldendeBeregningFom?.year == simuleringsaar) {
            simuleringsresultat.currentUforetrygdSummary.sumYtelseskomponenter ?: 0
        } else {
            simuleringsresultat.currentUforetrygdSummary.totalbelopNettoAr?.toInt() ?: 0
        }
    }

    private fun getSumArligUforetrygdSimulert(simuleringsresultat: SimulerEndringUforetrygdResponse): Int {
        return simuleringsresultat.simulertUforetrygdSummary.sumYtelseskomponenter ?: 0
    }
}