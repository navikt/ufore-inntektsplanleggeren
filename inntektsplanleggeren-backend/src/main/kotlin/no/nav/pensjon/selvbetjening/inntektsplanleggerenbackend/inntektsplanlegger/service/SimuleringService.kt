package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.service

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.ForventedeInntekterSummary
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.dto.BeforeAndAfterValues
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.dto.ForventedeInntekter
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.dto.Simuleringsresultat
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.PenClient
import org.springframework.stereotype.Service

@Service
class SimuleringService(val penClient: PenClient) {
    fun simulerInntektsendring(
        forventedeInntekterOppgitt: ForventedeInntekter,
        forventedeInntekter: ForventedeInntekterSummary
    ): Simuleringsresultat {
        val simuleringsresultat = penClient.simulerInntektsendring()
        return Simuleringsresultat(
            uforetrygd = BeforeAndAfterValues(0, 0),
            forventetInntekt = BeforeAndAfterValues(
                before = forventedeInntekter.sumBenyttedeInntekterBruker,
                after = forventedeInntekterOppgitt.bruker.sum()),
            barnetillegg = BeforeAndAfterValues(0, 0),
            gjenlevendetillegg = BeforeAndAfterValues(0, 0),
            sum = BeforeAndAfterValues(0, 0),
            uforetrygdPerMaaned = 0
        )
    }
}