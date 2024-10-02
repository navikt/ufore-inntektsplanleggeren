package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.service

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.dto.ForventedeInntekter
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.PenClient
import org.springframework.stereotype.Service

@Service
class SimuleringService(val penClient: PenClient) {
    fun simulerInntektsendring(forventedeInntekterOppgitt: ForventedeInntekter, forventedeInntekter){
        val simuleringsresultat = penClient.simulerInntektsendring()
    }

    private fun calculateSumBenyttetInntekt(){

    }
}