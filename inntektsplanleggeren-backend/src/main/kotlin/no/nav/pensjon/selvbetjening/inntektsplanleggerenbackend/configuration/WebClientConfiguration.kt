package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.configuration

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.ProxyProvider
import java.net.URI


@Configuration
class WebClientConfiguration {

    @Bean("webClientProxy")
    fun webClientProxy(): WebClient = (System.getenv("HTTP_PROXY")
        ?.let { URI(it) }
        ?.run {
            WebClient.builder()
                .clientConnector(
                    ReactorClientHttpConnector(
                        HttpClient.create()
                            .proxy {
                                it
                                    .type(ProxyProvider.Proxy.HTTP)
                                    .host(host)
                                    .port(port)
                            }
                    )
                )
                .build()
        }
        ?: WebClient.builder().build())

    @Bean
    fun webClient(): WebClient = WebClient.builder()
        .clientConnector(ReactorClientHttpConnector(HttpClient.create()))
        .exchangeStrategies(ExchangeStrategies.builder().codecs { it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024) }.build())
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

}