package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.configuration

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutException
import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.*
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import reactor.util.retry.Retry
import reactor.util.retry.RetryBackoffSpec
import java.time.Duration

@Configuration
class WebClientConfiguration {
    private val logger = LoggerFactory.getLogger(WebClientConfiguration::class.java)

    @Bean
    fun webClient(): WebClient = WebClient.builder()
        .clientConnector(ReactorClientHttpConnector(httpClient))
        .exchangeStrategies(ExchangeStrategies.builder().codecs { it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024) }.build())
        .filter(logRequest())
        .build()

    // Konfigurasjon for å unngå brannmur-timouts når vi kaller FSS fra GCP.
    // Forklart her: https://github.com/nais/doc/blob/nav-gcp-fss-com/docs/workloads/how-to/gcp-fss-communication.md
    private val connectionProvider = ConnectionProvider.builder("onprem-pool")
        .maxConnections(200)
        .maxIdleTime(Duration.ofMinutes(55))
        .maxLifeTime(Duration.ofMinutes(59))
        .evictInBackground(Duration.ofMinutes(5))
        .build()

    private val httpClient = HttpClient.create(connectionProvider)
        .option(ChannelOption.SO_KEEPALIVE, true)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
        .responseTimeout(Duration.ofSeconds(10))

    private fun logRequest() = ExchangeFilterFunction.ofResponseProcessor { response ->
        Mono.deferContextual { ctx ->
            val mdcMap = ctx.getOrDefault("mdc", emptyMap<String, String>()) as Map<String, String>
            val allKv = mdcMap.map { kv -> kv(kv.key, kv.value) } + kv("status_code", response.statusCode().value())
            logger.info("Utgående: ${response.request().method} ${response.statusCode().value()} ${response.request().uri}", *allKv.toTypedArray())
            Mono.just(response)
        }
    }
}

val retryOnTimeout: RetryBackoffSpec = Retry.backoff(2, Duration.ofMillis(100))
    .filter { throwable ->
        generateSequence(throwable) { it.cause }.any { it is ReadTimeoutException }
    }

fun <T : Any> Mono<T>.withMdcContext(): Mono<T> {
    val mdc = MDC.getCopyOfContextMap()
    return this.contextWrite { ctx -> ctx.put("mdc", mdc) }
}