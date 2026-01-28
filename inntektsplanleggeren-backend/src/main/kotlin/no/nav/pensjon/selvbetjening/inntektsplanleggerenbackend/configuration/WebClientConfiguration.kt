package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.configuration

import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.*
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient

@Configuration
class WebClientConfiguration {
    private val logger = LoggerFactory.getLogger(WebClientConfiguration::class.java)

    @Bean
    fun webClient(): WebClient = WebClient.builder()
        .clientConnector(ReactorClientHttpConnector(HttpClient.create()))
        .exchangeStrategies(ExchangeStrategies.builder().codecs { it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024) }.build())
        .filter(logRequest())
        .build()

    private fun logRequest() = ExchangeFilterFunction.ofResponseProcessor { response ->
        Mono.deferContextual { ctx ->
            val mdcMap = ctx.getOrDefault("mdc", emptyMap<String, String>()) as Map<String, String>
            val allKv = mdcMap.map { kv -> kv(kv.key, kv.value) } + kv("status_code", response.statusCode().value())
            logger.info("Utgående: ${response.request().method} ${response.statusCode().value()} ${response.request().uri}", *allKv.toTypedArray())
            Mono.just(response)
        }
    }
}

fun <T : Any> Mono<T>.withMdcContext(): Mono<T> {
    val mdc = MDC.getCopyOfContextMap()
    return this.contextWrite { ctx -> ctx.put("mdc", mdc) }
}