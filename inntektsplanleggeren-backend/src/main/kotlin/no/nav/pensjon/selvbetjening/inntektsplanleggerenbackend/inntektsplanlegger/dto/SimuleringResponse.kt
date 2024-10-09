package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.dto

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation.InntektsplanleggerMessage

data class SimuleringResponse(
    val messages: List<InntektsplanleggerMessage>,
    val result: Simuleringsresultat?
)

data class Simuleringsresultat(
    val uforetrygd: SimuleringAmounts,
    val forventetInntekt: SimuleringAmounts,
    val barnetilleggFellesbarn: SimuleringAmounts?,
    val barnetilleggSaerkullsbarn: SimuleringAmounts?,
    val gjenlevendetillegg: SimuleringAmounts?,
    val sum: SimuleringAmounts
)

data class SimuleringAmounts(
    val monthly: BeforeAndAfterValues,
    val yearly: BeforeAndAfterValues
)

data class BeforeAndAfterValues(val before: Int, val after: Int)
