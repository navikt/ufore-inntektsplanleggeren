package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.configuration.AppId
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.dto.AbonnerteInntekterIdentOgPeriode
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.dto.HentAbonnerteInntekterBolkRequest
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.dto.HentAbonnerteInntekterBolkResponse
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.dto.HentForventetInntektResponse
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.ClientException
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.ForbiddenException
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security.TokenService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException

@Component
class InntektskomponentClient(
    @Value("\${inntektskomponenten.endpoint.url}") private val url: String,
    @Value("\${inntektskomponenten.scope}") private val scope: String,
    @Value("\${inntektskomponenten.audience}") private val audience: String,
    private val webClient: WebClient,
    private val tokenService: TokenService,
) {
    fun hentForventetInntekt(
        pid: String,
        inntektsAar: List<Int>
    ): HentForventetInntektResponse {
        //TODO: Implement this
        return HentForventetInntektResponse(null)
    }

    fun hentAbonnerteInntekterBolk(
        abonnerteInntekterIdentOgPeriodeListe: List<AbonnerteInntekterIdentOgPeriode>,
        ainntektsfilter: String,
        formaal: String
    ): HentAbonnerteInntekterBolkResponse {
        val path = "/api/v1/hentabonnerteinntekterbolk"
        val request = HentAbonnerteInntekterBolkRequest(
            ainntektsfilter,
            null,           // filterversion, use null
            formaal,
            abonnerteInntekterIdentOgPeriodeListe
        )
        try {
            return tokenService.getEgressToken(scope = scope, audience = audience, "", AppId.INNTEKTSKOMPONENTEN)
                .let { accessToken ->
                    webClient
                        .post()
                        .uri("$url$path")
                        .header("Authorization", "Bearer $accessToken")
                        .accept(MediaType.APPLICATION_JSON)
                        .bodyValue(request)
                        .retrieve()
                        .bodyToMono(HentAbonnerteInntekterBolkResponse::class.java)
                        .block()!!
                }
        } catch (e: WebClientResponseException) {
            if (HttpStatus.FORBIDDEN == e.statusCode) {
                throw ForbiddenException(AppId.INNTEKTSKOMPONENTEN.name, path, e.message, e)
            }
            throw ClientException(AppId.INNTEKTSKOMPONENTEN.name, path, e.message, e)
        } catch (e: Exception) {
            throw ClientException(AppId.INNTEKTSKOMPONENTEN.name, path, e.message, e)
        }
    }
}