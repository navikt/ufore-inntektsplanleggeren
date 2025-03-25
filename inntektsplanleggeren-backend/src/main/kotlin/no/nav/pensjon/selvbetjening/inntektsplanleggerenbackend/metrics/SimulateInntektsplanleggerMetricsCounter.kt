package no.nav.pensjon.selvbetjening.alderspensjonendringssoknadbackend.metrics

import io.micrometer.core.instrument.Metrics
import no.nav.pensjon.selvbetjening.alderspensjonendringssoknadbackend.metrics.InitiateInntektsplanleggerMetricsCounter.Companion
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.InntektsplanleggerenInitialResponse
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.simulering.SimuleringResponse
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation.InntektsplanleggerMessageType
import org.slf4j.LoggerFactory

class SimulateInntektsplanleggerMetricsCounter {
    companion object{
        private const val EVENT_NAME = "inntektsplanlegger_simulate"
        private val logger = LoggerFactory.getLogger(SimulateInntektsplanleggerMetricsCounter::class.java)

        fun count(initiateResponse: SimuleringResponse){
            try {
                if(initiateResponse.messages.any { it.messageCode.type == InntektsplanleggerMessageType.ERROR }){
                    countEvent("SIMULATE_ERROR")
                } else {
                    countEvent("SIMULATE_SUCCESS")
                }
            } catch(e: Exception){
                logger.error("Failed counting soknad status")
            }
        }

        private fun countEvent(result: String) {
            Metrics.counter(EVENT_NAME, "result", result).increment()
        }
    }
}