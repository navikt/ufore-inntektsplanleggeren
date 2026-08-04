package no.nav.ufore.inntektsplanleggeren.metrics

import io.micrometer.core.instrument.Metrics
import no.nav.ufore.inntektsplanleggeren.inntektsplanlegger.InntektsplanleggerenStatusResponse
import org.slf4j.LoggerFactory

class StatusInntektsplanleggerMetricsCounter {
    companion object{
        private const val EVENT_NAME = "inntektsplanlegger_status"
        private val logger = LoggerFactory.getLogger(StatusInntektsplanleggerMetricsCounter::class.java)

        fun count(statusResponse: InntektsplanleggerenStatusResponse?){
            try {
                if (statusResponse != null) {
                    countEvent("STATUS_SUCCESS.${statusResponse.status}")
                } else {
                    countEvent("STATUS_WAITING")
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