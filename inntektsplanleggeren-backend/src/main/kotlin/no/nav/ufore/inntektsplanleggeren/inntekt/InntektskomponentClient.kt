package no.nav.ufore.inntektsplanleggeren.inntekt

import no.nav.ufore.inntektsplanleggeren.configuration.AppId
import no.nav.ufore.inntektsplanleggeren.configuration.retryOnTimeout
import no.nav.ufore.inntektsplanleggeren.configuration.withMdcContext
import no.nav.ufore.inntektsplanleggeren.util.getCurrentCallId
import no.nav.ufore.inntektsplanleggeren.inntekt.dto.AbonnerteInntekterIdentOgPeriode
import no.nav.ufore.inntektsplanleggeren.inntekt.dto.HentAbonnerteInntekterBolkRequest
import no.nav.ufore.inntektsplanleggeren.inntekt.dto.HentAbonnerteInntekterBolkResponse
import no.nav.ufore.inntektsplanleggeren.inntekt.dto.HentForventetInntektResponse
import no.nav.ufore.inntektsplanleggeren.inntektsplanlegger.ClientException
import no.nav.ufore.inntektsplanleggeren.inntektsplanlegger.ForbiddenException
import no.nav.ufore.inntektsplanleggeren.inntektsplanlegger.ManglerTilgangInntektskomponentenException
import no.nav.ufore.inntektsplanleggeren.inntektsplanlegger.PersonNotFoundException
import no.nav.ufore.inntektsplanleggeren.security.TokenService
import no.nav.ufore.inntektsplanleggeren.util.NAV_CALL_ID_HEADER
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
    private val webClient: WebClient,
    private val tokenService: TokenService
) {
    private val logger: Logger = LoggerFactory.getLogger(InntektskomponentClient::class.java)

    fun hentForventetInntekt(
        pid: String,
        inntektsAar: List<Int>
    ): HentForventetInntektResponse {
        throw ManglerTilgangInntektskomponentenException(AppId.INNTEKTSKOMPONENTEN.name, "", "", null)
        val path = "/rs/api/v1/forventetinntekt"
        try {
            return tokenService.getEgressToken(scope = scope, pid = pid, appId = AppId.INNTEKTSKOMPONENTEN)
                .let { accessToken ->
                    webClient
                        .get()
                        .uri("$url$path")
                        .header("Authorization", "Bearer $accessToken")
                        .header("norskident", pid)
                        .header("aar-list",inntektsAar.joinToString(","))
                        .header(NAV_CALL_ID_HEADER, getCurrentCallId())
                        .header("Nav-Consumer_id", "ufoere")            //avtalt med team inntekt
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .bodyToMono(HentForventetInntektResponse::class.java)
                        .retryWhen(retryOnTimeout)
                        .withMdcContext()
                        .block()!!
                }
        } catch (e: WebClientResponseException) {
            when (e.statusCode) {
                HttpStatus.FORBIDDEN -> throw ManglerTilgangInntektskomponentenException(AppId.INNTEKTSKOMPONENTEN.name, path, e.message, e)
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
        formaal: String,
        pid: String
    ): HentAbonnerteInntekterBolkResponse {
        val path = "/rs/api/v1/hentabonnerteinntekterbolk"
        val request = HentAbonnerteInntekterBolkRequest(
            ainntektsfilter,
            null,           // filterversion, use null
            formaal,
            abonnerteInntekterIdentOgPeriodeListe
        )
        try {
            return tokenService.getEgressToken(scope = scope, pid = pid, appId = AppId.INNTEKTSKOMPONENTEN)
                .let { accessToken ->
                    webClient
                        .post()
                        .uri("$url$path")
                        .header("Authorization", "Bearer $accessToken")
                        .header(NAV_CALL_ID_HEADER, getCurrentCallId())
                        .header("Nav-Consumer_id", "ufoere")        //avtalt med team inntekt
                        .accept(MediaType.APPLICATION_JSON)
                        .bodyValue(request)
                        .retrieve()
                        .bodyToMono(HentAbonnerteInntekterBolkResponse::class.java)
                        .retryWhen(retryOnTimeout)
                        .withMdcContext()
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