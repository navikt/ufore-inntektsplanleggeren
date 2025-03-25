package no.nav.pensjon.selvbetjening.alderspensjonendringssoknadbackend.metrics

import io.micrometer.core.instrument.Metrics
import no.nav.pensjon.selvbetjening.alderspensjonendringssoknadbackend.metrics.InitiateInntektsplanleggerMetricsCounter.Companion
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.InnsendingStatus
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.InntektsplanleggerenInitialResponse
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.InntektsplanleggerenSendResponse
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.simulering.SimuleringResponse
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation.InntektsplanleggerMessageType
import org.slf4j.LoggerFactory

class SendInntektsplanleggerMetricsCounter {
    companion object{
        private const val EVENT_NAME = "inntektsplanlegger_send"
        private val logger = LoggerFactory.getLogger(SendInntektsplanleggerMetricsCounter::class.java)

        fun count(sendResponse: InntektsplanleggerenSendResponse){
            try {
                if(sendResponse.messages.any { it.messageCode.type == InntektsplanleggerMessageType.ERROR }){
                    countEvent("SEND_ERROR")
                } else {
                    countEvent("SEND_SUCCESS.${sendResponse.status}")
                }
            } catch(e: Exception) {
                logger.error("Failed counting soknad status")
            }
        }

        private fun countEvent(result: String) {
            Metrics.counter(EVENT_NAME, "result", result).increment()
        }
    }
}