package no.nav.pensjon.selvbetjening.alderspensjonendringssoknadbackend.metrics

import io.micrometer.core.instrument.Metrics
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.simulering.SimuleringResponse
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation.InntektsplanleggerMessageType
import org.slf4j.LoggerFactory

class SimulateInntektsplanleggerMetricsCounter {
    companion object{
        private const val EVENT_NAME = "inntektsplanlegger_simulate"
        private val logger = LoggerFactory.getLogger(SimulateInntektsplanleggerMetricsCounter::class.java)

        fun count(simuleringResponse: SimuleringResponse){
            try {
                if(simuleringResponse.messages.none { it.type == InntektsplanleggerMessageType.ERROR }) {
                    countEvent("SIMULATE_SUCCESS")
                } else {
                    for (message in simuleringResponse.messages) {
                        if (message.type == InntektsplanleggerMessageType.ERROR) {
                            countEvent("SIMULATE_ERROR.${message.messageCode}")
                        }
                    }
                }
            } catch(e: Exception){
                logger.error("Failed counting simulate status")
            }
        }

        private fun countEvent(result: String) {
            Metrics.counter(EVENT_NAME, "result", result).increment()
        }
    }
}

