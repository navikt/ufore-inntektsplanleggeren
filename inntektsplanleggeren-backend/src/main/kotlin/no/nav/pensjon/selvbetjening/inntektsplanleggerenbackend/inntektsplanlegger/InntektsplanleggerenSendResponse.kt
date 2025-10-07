package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation.InntektsplanleggerMessage
import java.time.LocalDateTime

data class InntektsplanleggerenSendResponse(val messages: List<InntektsplanleggerMessage>,
                                            val status: InnsendingStatus,
                                            @JsonProperty("innsendingsTidspunkt")
                                            @JsonFormat(pattern = "yyyy-MM-dd' 'HH:mm:ss")
                                            val innsendingsTidspunkt: LocalDateTime)

enum class InnsendingStatus{
    AUTOMATISK_BEHANDLING,
    INNTEKT_LAGRET_INGEN_BEHANDLING,
    IKKE_SENDT_VALIDERING_FEILET,
    IKKE_SENDT
}