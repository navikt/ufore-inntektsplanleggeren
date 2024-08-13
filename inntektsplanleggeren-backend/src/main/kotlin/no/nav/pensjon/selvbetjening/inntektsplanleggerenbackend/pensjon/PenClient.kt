package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.configuration.AppId
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.configuration.CallIdUtil
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.configuration.getCallIdFromMdc
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.fullmakt.FullmaktClient.Companion.NAV_CALL_ID
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security.TokenService
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.planlegger.ForbiddenException
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.planlegger.ClientException
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException

@Component
class PenClient(
    @Value("\${pen.endpoint.url}") private val url: String,
    @Value("\${pen.scope}") private val scope: String,
    @Value("\${pen.audience}") private val audience: String,
    private val webClient: WebClient,
    private val tokenService: TokenService
) {

    fun getHasLopendeBeregningsperiode(pid: String): Map<String, Boolean> {
        val path = "/pen/api/selvbetjening/inntektsplanleggeren/lopendeBeregningsperiode"
        try {
            return tokenService.getEgressToken(scope = scope, audience = audience, pid = pid, appId = AppId.PEN)
                .let { accessToken ->
                    webClient
                        .get()
                        .uri("$url$path")
                        .header("fnr", pid)
                        .header("Authorization", "Bearer $accessToken")
                        .header(NAV_CALL_ID, CallIdUtil.getCallIdFromMdc())
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .bodyToMono(object : ParameterizedTypeReference<Map<String, Boolean>>() {})
                        .block() ?: emptyMap()
                }
        } catch (e: WebClientResponseException) {
            if (HttpStatus.FORBIDDEN == e.statusCode) {
                throw ForbiddenException(AppId.PEN.name, path, e.message, e)
            }
            throw ClientException(AppId.PEN.name, path, e.message, e)
        } catch (e: Exception) {
            throw ClientException(AppId.PEN.name, path, e.message, e)
        }
    }

}