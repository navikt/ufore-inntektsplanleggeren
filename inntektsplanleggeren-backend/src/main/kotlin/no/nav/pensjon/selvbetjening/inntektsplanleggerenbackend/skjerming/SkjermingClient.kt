package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.skjerming

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.configuration.AppId
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.util.NAV_CALL_ID_HEADER
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.util.getCurrentCallId
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security.TokenService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class SkjermingClient(
    @Value("\${skjerming.endpoint.url}") private val url: String,
    private val webClient: WebClient,
    @Value("\${skjerming.scope}") private val scope: String,
    private val tokenService: TokenService
) {
    fun isSkjermet(pid: String): Boolean {
        return tokenService.getEgressToken(scope = scope, pid = pid, appId = AppId.SKJERMING).let { accessToken ->
            webClient
                .post()
                .uri("$url/skjermet")
                .header("Authorization", "Bearer $accessToken")
                .header(NAV_CALL_ID_HEADER, getCurrentCallId())
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(SkjermingRequest(pid))
                .retrieve()
                .bodyToMono(Boolean::class.java)
                .block() ?: false
        }
    }
}