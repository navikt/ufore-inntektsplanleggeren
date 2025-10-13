package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.configuration

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
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
        .filter(putMdcOnContext())
        .filter(logRequest())
        .build()

    @Bean
    fun objectMapper(): ObjectMapper {
        return ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
            .registerModule(JavaTimeModule())
            .registerModule(KotlinModule.Builder()
                .withReflectionCacheSize(512)
                .configure(KotlinFeature.NullToEmptyCollection, false)
                .configure(KotlinFeature.NullToEmptyMap, false)
                .configure(KotlinFeature.NullIsSameAsDefault, false)
                .configure(KotlinFeature.SingletonSupport, false)
                .configure(KotlinFeature.StrictNullChecks, false)
                .build())
    }

    private fun putMdcOnContext() = ExchangeFilterFunction { request, next ->
        val mdc = MDC.getCopyOfContextMap()
        next.exchange(request)
            .contextWrite { ctx -> ctx.put("mdc", mdc) }
    }

    private fun logRequest() = ExchangeFilterFunction.ofResponseProcessor { response ->
        Mono.deferContextual { ctx ->
            val mdcMap = ctx.getOrDefault("mdc", emptyMap<String, String>()) as Map<String, String>
            val allKv = mdcMap.map { kv -> kv(kv.key, kv.value) } + kv("status_code", response.statusCode().value())
            logger.info("${response.request().method} ${response.statusCode().value()} ${response.request().uri}", *allKv.toTypedArray())
            Mono.just(response)
        }
    }

}