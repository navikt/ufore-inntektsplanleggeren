package no.nav.pensjon.selvbetjening.alderspensjonendringssoknadbackend.metrics

import io.micrometer.core.instrument.Metrics
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.InntektsplanleggerenInitialResponse
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation.InntektsplanleggerMessageType
import org.slf4j.LoggerFactory

class InitiateInntektsplanleggerMetricsCounter {
    companion object{
        private const val EVENT_NAME = "inntektsplanlegger_initiate"
        private val logger = LoggerFactory.getLogger(InntektsplanleggerenInitialResponse::class.java)

        fun count(initiateResponse: InntektsplanleggerenInitialResponse){
            try {
                if(initiateResponse.messages.none { it.type == InntektsplanleggerMessageType.ERROR }) {
                    countEvent("INITIATE_SUCCESS")
                } else {
                    for (message in initiateResponse.messages) {
                        if (message.type == InntektsplanleggerMessageType.ERROR) {
                            countEvent("INITIATE_ERROR.${message.messageCode}")
                        }
                    }
                }

            } catch(e: Exception){
                logger.error("Failed counting initiate status")
            }
        }

        private fun countEvent(result: String) {
            Metrics.counter(EVENT_NAME, "result", result).increment()
        }
    }
}