package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.simulering

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.ForventedeInntekterSummary
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.inntekt.ForventedeInntekter
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation.SimuleringValidator
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation.Validator
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.PenClient
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.SimulerEndringUforetrygdResponse
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.Ytelseskomponent
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security.TokenService
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class SimuleringService(
    private val penClient: PenClient,
    private val validator: SimuleringValidator,
    private val tokenService: TokenService
) {
    fun simulerInntektsendring(
        pid: String,
        forventedeInntekterOppgitt: ForventedeInntekter,
        forventedeInntekter: ForventedeInntekterSummary,
        simuleringsaar: Int,
        simuleringFomDato: LocalDate
    ): SimuleringData {
        val initiertAv = tokenService.determineLoggedInUser()
        val simuleringsresultat = penClient.simulerInntektsendring(
            pid = pid,
            virk = simuleringFomDato,
            inntektsgrunnlagListe = forventedeInntekterOppgitt.bruker.mapToInntektsgrunnlag(simuleringsaar, initiertAv),
            inntektsgrunnlagListeEps = forventedeInntekterOppgitt.eps?.mapToInntektsgrunnlag(simuleringsaar, initiertAv)
                ?: emptyList()
        )
        val valideringsresultat = validator.validateSimulering(
            simuleringsresultat,
            forventedeInntekterOppgitt,
            simuleringFomDato,
            simuleringsaar,
            pid
        )
        return SimuleringData(
            valideringsresultat = valideringsresultat, simuleringsresultat = Simuleringsresultat(
                uforetrygd = convertYtelseskomponenterToSimuleringAmounts(
                    simuleringsresultat.currentUforetrygdSummary.uforetrygdYtelseskomponenter.uforetrygdOrdiner,
                    simuleringsresultat.simulertUforetrygdSummary.uforetrygdYtelseskomponenter.uforetrygdOrdiner
                )!!,
                forventetInntekt = SimuleringAmounts(
                    yearly = BeforeAndAfterValues(
                        before = forventedeInntekter.sumBenyttedeInntekterBruker,
                        after = forventedeInntekterOppgitt.bruker.sum()
                    ), monthly = BeforeAndAfterValues(
                        before = forventedeInntekter.sumBenyttedeInntekterBruker / 12,
                        after = forventedeInntekterOppgitt.bruker.sum() / 12
                    )
                ),
                barnetilleggFellesbarn = convertYtelseskomponenterToSimuleringAmounts(
                    simuleringsresultat.currentUforetrygdSummary.uforetrygdYtelseskomponenter.barnetilleggFellesbarn,
                    simuleringsresultat.simulertUforetrygdSummary.uforetrygdYtelseskomponenter.barnetilleggFellesbarn
                ),
                barnetilleggSaerkullsbarn = convertYtelseskomponenterToSimuleringAmounts(
                    simuleringsresultat.currentUforetrygdSummary.uforetrygdYtelseskomponenter.barnetilleggSaerkullsbarn,
                    simuleringsresultat.simulertUforetrygdSummary.uforetrygdYtelseskomponenter.barnetilleggSaerkullsbarn
                ),
                gjenlevendetillegg = convertYtelseskomponenterToSimuleringAmounts(
                    simuleringsresultat.currentUforetrygdSummary.uforetrygdYtelseskomponenter.gjenlevendetillegg,
                    simuleringsresultat.simulertUforetrygdSummary.uforetrygdYtelseskomponenter.gjenlevendetillegg
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

    private fun convertYtelseskomponenterToSimuleringAmounts(
        ytelseskomponentBefore: Ytelseskomponent?,
        ytelseskomponentAfter: Ytelseskomponent?
    ): SimuleringAmounts? {

        if (ytelseskomponentBefore == null && ytelseskomponentAfter == null) {
            return null
        }
        return SimuleringAmounts(
            yearly = BeforeAndAfterValues(
                before = ytelseskomponentBefore?.amountPerYear ?: 0,
                after = ytelseskomponentAfter?.amountPerYear ?: 0
            ),
            monthly = BeforeAndAfterValues(
                before = ytelseskomponentBefore?.amountPerMonth ?: 0,
                after = ytelseskomponentAfter?.amountPerMonth ?: 0
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