package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.configuration.withMdcContext
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.util.getCurrentCallId
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.fullmakt.FullmaktClient.Companion.NAV_CALL_ID
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class PidEncryptionClient(
    @Value("\${pid-encryption.endpoint.url}") private val baseUrl: String,
    @Value("\${pid-encryption.scope}") private val scope: String,
    private val azureAdService: AzureAdService,
    private val webClient: WebClient,
) {
    fun decrypt(encryptedPid: String): String? =
        azureAdService.retrieveClientCredentialsToken(listOf(scope)).let { token ->
            webClient
                .post()
                .uri("$baseUrl/api/decrypt")
                .header("Authorization", "Bearer $token")
                .header(NAV_CALL_ID, getCurrentCallId())
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(encryptedPid)
                .retrieve()
                .bodyToMono(String::class.java)
                .withMdcContext()
                .block()
        }
}
