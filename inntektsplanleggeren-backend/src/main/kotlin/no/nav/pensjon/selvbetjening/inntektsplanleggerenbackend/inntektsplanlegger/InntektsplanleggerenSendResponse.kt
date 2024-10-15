package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation.InntektsplanleggerMessage

data class InntektsplanleggerenSendResponse(val messages: List<InntektsplanleggerMessage>, val status: InnsendingStatus)

enum class InnsendingStatus{
    AUTOMATISK_BEHANDLING,
    INNTEKT_LAGRET_INGEN_BEHANDLING,
    IKKE_SENDT_VALIDERING_FEILET,
    IKKE_SENDT
}