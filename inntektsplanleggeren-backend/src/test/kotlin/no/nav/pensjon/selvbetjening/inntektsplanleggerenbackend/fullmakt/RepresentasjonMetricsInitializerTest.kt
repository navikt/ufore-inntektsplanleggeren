package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.fullmakt

import io.micrometer.core.instrument.Metrics
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class RepresentasjonMetricsInitializerTest {

    private lateinit var meterRegistry: SimpleMeterRegistry

    @BeforeEach
    fun setup() {
        meterRegistry = SimpleMeterRegistry()
        Metrics.addRegistry(meterRegistry)
    }

    @AfterEach
    fun cleanup() {
        Metrics.removeRegistry(meterRegistry)
        meterRegistry.clear()
    }

    @Test
    fun `should pre-register all obo outcome counters for reachable methods`() {
        RepresentasjonMetricsInitializer().registerCounters()

        for (outcome in OboTilgangOutcome.entries) {
            for (method in RepresentasjonMetricsInitializer.OBO_HTTP_METHODS) {
                val counter = meterRegistry.find(OBO_TILGANG_EVENT)
                    .tag("outcome", outcome.tag)
                    .tag("method", method)
                    .counter()

                assertNotNull(counter)
                assertEquals(0.0, counter.count())
            }
        }

        assertNull(
            meterRegistry.find(OBO_TILGANG_EVENT)
                .tag("outcome", OboTilgangOutcome.INNVILGET.tag)
                .tag("method", "DELETE")
                .counter()
        )
    }
}
