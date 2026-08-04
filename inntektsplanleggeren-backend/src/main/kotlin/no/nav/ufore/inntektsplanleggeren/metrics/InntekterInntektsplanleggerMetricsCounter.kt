package no.nav.ufore.inntektsplanleggeren.metrics

import io.micrometer.core.instrument.Metrics
import org.slf4j.LoggerFactory

class InntekterInntektsplanleggerMetricsCounter {
    companion object{
        private const val EVENT_NAME = "inntektsplanlegger_inntekter"
        private val logger = LoggerFactory.getLogger(InntekterInntektsplanleggerMetricsCounter::class.java)

        fun count(){
            try {
                countEvent("INNTEKTER_SUCCESS")

            } catch(e: Exception){
                logger.error("Failed counting inntekter status")
            }
        }

        private fun countEvent(result: String) {
            Metrics.counter(EVENT_NAME, "result", result).increment()
        }
    }
}