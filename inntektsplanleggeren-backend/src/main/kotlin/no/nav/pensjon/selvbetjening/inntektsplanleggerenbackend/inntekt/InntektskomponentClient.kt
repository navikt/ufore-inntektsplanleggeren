package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.configuration.AppId
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.configuration.CallIdUtil
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.configuration.getCallIdFromMdc
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.fullmakt.FullmaktClient.Companion.NAV_CALL_ID
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.dto.AbonnerteInntekterIdentOgPeriode
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.dto.HentAbonnerteInntekterBolkRequest
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.dto.HentAbonnerteInntekterBolkResponse
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.dto.HentForventetInntektResponse
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.ClientException
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.ForbiddenException
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.PersonNotFoundException
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security.AzureAdService
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security.TokenService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
    private val azureAdService: AzureAdService
) {
    private val logger: Logger = LoggerFactory.getLogger(InntektskomponentClient::class.java)

    fun hentForventetInntekt(
        pid: String,
        inntektsAar: List<Int>
    ): HentForventetInntektResponse {
        val path = "/api/v1/forventetinntekt"
        try {
            return azureAdService.retrieveClientCredentialsToken(listOf(scope)) //TODO: Temp fix original code: tokenService.getEgressToken(scope = scope, audience = audience, "", AppId.INNTEKTSKOMPONENTEN)
                .let { accessToken ->
                    webClient
                        .get()
                        .uri("$url$path")
                        .header("Authorization", "Bearer $accessToken")
                        .header("norskident", pid)
                        .header("aar-list",inntektsAar.joinToString(","))
                        .header(NAV_CALL_ID, CallIdUtil.getCallIdFromMdc())
                        .header("Nav-Consumer_id", "ufoere")            //avtalt med team inntekt
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .bodyToMono(HentForventetInntektResponse::class.java)
                        .block()!!
                }
        } catch (e: WebClientResponseException) {
            when (e.statusCode) {
                HttpStatus.FORBIDDEN -> throw ForbiddenException(AppId.INNTEKTSKOMPONENTEN.name, path, e.message, e)
                HttpStatus.NOT_FOUND  -> throw PersonNotFoundException(AppId.INNTEKTSKOMPONENTEN.name, path, e.message, e)
                HttpStatus.BAD_REQUEST  -> {
                    logger.error("Bad request "+ e.responseBodyAsString)
                    throw ClientException(AppId.INNTEKTSKOMPONENTEN.name, path, e.message, e)}
                else -> throw ClientException(AppId.INNTEKTSKOMPONENTEN.name, path, e.message, e)
            }
        } catch (e: Exception) {
            throw ClientException(AppId.INNTEKTSKOMPONENTEN.name, path, e.message, e)
        }
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
                        .header(NAV_CALL_ID, CallIdUtil.getCallIdFromMdc())
                        .header("Nav-Consumer_id", "ufoere")        //avtalt med team inntekt
                        .accept(MediaType.APPLICATION_JSON)
                        .bodyValue(request)
                        .retrieve()
                        .bodyToMono(HentAbonnerteInntekterBolkResponse::class.java)
                        .block()!!
                }
        } catch (e: WebClientResponseException) {
            when (e.statusCode) {
                HttpStatus.FORBIDDEN -> throw ForbiddenException(AppId.INNTEKTSKOMPONENTEN.name, path, e.message, e)
                HttpStatus.NOT_FOUND  -> throw PersonNotFoundException(AppId.INNTEKTSKOMPONENTEN.name, path, e.message, e)
                HttpStatus.BAD_REQUEST  -> {
                    logger.error("Bad request "+ e.responseBodyAsString)
                    throw ClientException(AppId.INNTEKTSKOMPONENTEN.name, path, e.message, e)}
                else -> throw ClientException(AppId.INNTEKTSKOMPONENTEN.name, path, e.message, e)
            }
        } catch (e: Exception) {
            throw ClientException(AppId.INNTEKTSKOMPONENTEN.name, path, e.message, e)
        }
    }
}