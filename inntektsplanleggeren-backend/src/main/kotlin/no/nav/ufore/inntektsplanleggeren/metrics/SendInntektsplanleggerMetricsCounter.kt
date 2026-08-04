package no.nav.ufore.inntektsplanleggeren.metrics

import io.micrometer.core.instrument.Metrics
import no.nav.ufore.inntektsplanleggeren.inntektsplanlegger.InntektsplanleggerenSendResponse
import no.nav.ufore.inntektsplanleggeren.inntektsplanlegger.validation.InntektsplanleggerMessageType
import org.slf4j.LoggerFactory

class SendInntektsplanleggerMetricsCounter {
    companion object{
        private const val EVENT_NAME = "inntektsplanlegger_send"
        private val logger = LoggerFactory.getLogger(SendInntektsplanleggerMetricsCounter::class.java)

        fun count(sendResponse: InntektsplanleggerenSendResponse){
            try {
                if(sendResponse.messages.none() { it.type == InntektsplanleggerMessageType.ERROR }) {
                    countEvent("SEND_SUCCESS.${sendResponse.status}")
                } else {
                    for (message in sendResponse.messages) {
                        if (message.type == InntektsplanleggerMessageType.ERROR) {
                            countEvent("SEND_ERROR.${message.messageCode}")
                        }
                    }
                }
            } catch(e: Exception) {
                logger.error("Failed counting send status")
            }
        }

        private fun countEvent(result: String) {
            Metrics.counter(EVENT_NAME, "result", result).increment()
        }
    }
}
