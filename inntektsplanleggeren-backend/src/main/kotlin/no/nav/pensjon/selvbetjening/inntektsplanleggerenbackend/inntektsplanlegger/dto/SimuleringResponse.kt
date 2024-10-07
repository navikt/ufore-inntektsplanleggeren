package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.dto

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation.InntektsplanleggerMessage

data class SimuleringResponse(
    val messages: List<InntektsplanleggerMessage>,
    val result: Simuleringsresultat?
)

data class Simuleringsresultat(
    val uforetrygd: BeforeAndAfterValues,
    val forventetInntekt: BeforeAndAfterValues,
    val barnetilleggFellesbarn: BeforeAndAfterValues?,
    val barnetilleggSaerkullsbarn: BeforeAndAfterValues?,
    val gjenlevendetillegg: BeforeAndAfterValues?,
    val sum: BeforeAndAfterValues,
    val uforetrygdPerMaaned: Int
)

data class BeforeAndAfterValues(val before: Int, val after: Int)
