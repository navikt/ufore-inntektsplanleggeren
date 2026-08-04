package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.fullmakt

import io.micrometer.core.instrument.Metrics
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

const val OBO_TILGANG_EVENT = "representasjon_obo_tilgang"

/** Value for the structured `event` log field, used to filter/aggregate in OpenSearch. */
const val EVENT_OBO_AVVIST = "obo_tilgang_avvist"

enum class OboTilgangOutcome(val tag: String) {
    INNVILGET("innvilget"),
    INGEN_GYLDIG_REPRESENTASJON("ingen_gyldig_representasjon"),
    ADRESSEBESKYTTELSE("adressebeskyttelse"),
    FULLMAKT_FEIL("fullmakt_feil")
}

fun countOboTilgang(outcome: OboTilgangOutcome, httpMethod: String) =
    Metrics.counter(OBO_TILGANG_EVENT, "outcome", outcome.tag, "method", httpMethod).increment()

/**
 * Registers every known counter/tag combination with value 0 at startup.
 *
 * Representasjon is a rare event - there can be hours between requests. Without this,
 * the counters would not exist in /actuator/prometheus until the first OBO request,
 * making "no data" in Grafana ambiguous.
 *
 * Runs on ApplicationReadyEvent, not at construction, because the Prometheus registry
 * must be attached to the global composite registry before meters are registered.
 */
@Component
class RepresentasjonMetricsInitializer {

    @EventListener(ApplicationReadyEvent::class)
    fun registerCounters() {
        for (outcome in OboTilgangOutcome.entries) {
            for (method in OBO_HTTP_METHODS) {
                Metrics.counter(OBO_TILGANG_EVENT, "outcome", outcome.tag, "method", method)
            }
        }
    }

    companion object {
        val OBO_HTTP_METHODS = listOf("GET", "POST")
    }
}
