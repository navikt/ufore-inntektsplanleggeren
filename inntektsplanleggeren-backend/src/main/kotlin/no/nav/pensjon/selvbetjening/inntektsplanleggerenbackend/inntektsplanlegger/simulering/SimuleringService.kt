package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.simulering

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.ForventedeInntekterSummary
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.inntekt.ForventedeInntekter
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation.SimuleringValidator
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.PenClient
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.SimulerEndringUforetrygdResponse
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.Ytelseskomponent
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security.TokenService
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.util.NowProvider
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class SimuleringService(
    private val penClient: PenClient,
    private val validator: SimuleringValidator,
    private val tokenService: TokenService,
    private val nowProvider: NowProvider
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
                    simuleringsresultat.simulertUforetrygdSummary.uforetrygdYtelseskomponenter.uforetrygdOrdiner,
                    simuleringsaar
                )!!,
                forventetInntekt = SimuleringAmounts(
                    yearly = BeforeAndAfterValues(
                        before = if (isSimuleringsaarThisYear(simuleringsaar)) forventedeInntekter.sumBenyttedeInntekterBruker else null,
                        after = forventedeInntekterOppgitt.bruker.sum()
                    ), monthly = BeforeAndAfterValues(
                        before = if (isSimuleringsaarThisYear(simuleringsaar)) forventedeInntekter.sumBenyttedeInntekterBruker / 12 else null,
                        after = forventedeInntekterOppgitt.bruker.sum() / 12
                    )
                ),
                barnetilleggFellesbarn = convertYtelseskomponenterToSimuleringAmounts(
                    simuleringsresultat.currentUforetrygdSummary.uforetrygdYtelseskomponenter.barnetilleggFellesbarn,
                    simuleringsresultat.simulertUforetrygdSummary.uforetrygdYtelseskomponenter.barnetilleggFellesbarn,
                    simuleringsaar
                ),
                barnetilleggSaerkullsbarn = convertYtelseskomponenterToSimuleringAmounts(
                    simuleringsresultat.currentUforetrygdSummary.uforetrygdYtelseskomponenter.barnetilleggSaerkullsbarn,
                    simuleringsresultat.simulertUforetrygdSummary.uforetrygdYtelseskomponenter.barnetilleggSaerkullsbarn,
                    simuleringsaar
                ),
                gjenlevendetillegg = convertYtelseskomponenterToSimuleringAmounts(
                    simuleringsresultat.currentUforetrygdSummary.uforetrygdYtelseskomponenter.gjenlevendetillegg,
                    simuleringsresultat.simulertUforetrygdSummary.uforetrygdYtelseskomponenter.gjenlevendetillegg,
                    simuleringsaar
                ),
                sum = SimuleringAmounts(
                    yearly = BeforeAndAfterValues(
                        before = getSumArligUforetrygdAndInntekt(simuleringsresultat, forventedeInntekter, simuleringsaar),
                        after = getSumArligUforetrygdSimulertAndInntekt(simuleringsresultat, forventedeInntekterOppgitt)
                    ), monthly = BeforeAndAfterValues(
                        before = getSumMaanedligUforetrygdAndInntekt(simuleringsresultat, forventedeInntekter, simuleringsaar),
                        after = getSumMaanedligUforetrygdSimulertAndInntekt(simuleringsresultat, forventedeInntekterOppgitt)
                    )
                )
            )
        )
    }

    private fun convertYtelseskomponenterToSimuleringAmounts(
        ytelseskomponentBefore: Ytelseskomponent?,
        ytelseskomponentAfter: Ytelseskomponent?,
        simuleringsaar: Int
    ): SimuleringAmounts? {

        if (ytelseskomponentBefore == null && ytelseskomponentAfter == null) {
            return null
        }
        return SimuleringAmounts(
            yearly = BeforeAndAfterValues(
                before = if (isSimuleringsaarThisYear(simuleringsaar)) ytelseskomponentBefore?.amountPerYear
                    ?: 0 else null,
                after = ytelseskomponentAfter?.amountPerYear ?: 0
            ),
            monthly = BeforeAndAfterValues(
                before = if (isSimuleringsaarThisYear(simuleringsaar)) ytelseskomponentBefore?.amountPerMonth
                    ?: 0 else null,
                after = ytelseskomponentAfter?.amountPerMonth ?: 0
            )
        )
    }

    private fun getSumMaanedligUforetrygdAndInntekt(simuleringsresultat: SimulerEndringUforetrygdResponse, forventedeInntekter: ForventedeInntekterSummary, simuleringsaar: Int) =
        if (isSimuleringsaarThisYear(simuleringsaar))
            (simuleringsresultat.currentUforetrygdSummary.totalbelopNetto ?: 0) + (forventedeInntekter.sumBenyttedeInntekterBruker / 12)
        else
            null

    private fun getSumMaanedligUforetrygdSimulertAndInntekt(simuleringsresultat: SimulerEndringUforetrygdResponse, forventedeInntekterOppgitt: ForventedeInntekter) =
        (simuleringsresultat.simulertUforetrygdSummary.totalbelopNetto ?: 0) + forventedeInntekterOppgitt.bruker.sum() / 12

    private fun getSumArligUforetrygdAndInntekt(
        simuleringsresultat: SimulerEndringUforetrygdResponse,
        forventedeInntekter: ForventedeInntekterSummary,
        simuleringsaar: Int
    ): Int? {
        return if (isSimuleringsaarThisYear(simuleringsaar)) {
            (simuleringsresultat.currentUforetrygdSummary.sumYtelseskomponenter ?: 0) + forventedeInntekter.sumBenyttedeInntekterBruker
        } else {
            null
        }
    }

    private fun getSumArligUforetrygdSimulertAndInntekt(simuleringsresultat: SimulerEndringUforetrygdResponse,
                                                        forventedeInntekterOppgitt: ForventedeInntekter): Int {
        return (simuleringsresultat.simulertUforetrygdSummary.sumYtelseskomponenter ?: 0) + forventedeInntekterOppgitt.bruker.sum()
    }

    private fun isSimuleringsaarThisYear(simuleringsaar: Int) =
        simuleringsaar == nowProvider.now().year
}