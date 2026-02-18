package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.configuration.AppId
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.configuration.withMdcContext
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.util.getCurrentCallId
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.fullmakt.FullmaktClient.Companion.NAV_CALL_ID
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.ClientException
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.ForbiddenException
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.*
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security.TokenService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class PenClient(
    @Value("\${pen.endpoint.url}") private val url: String,
    @Value("\${pen.scope}") private val scope: String,
    @Value("\${pen.audience}") private val audience: String,
    private val webClient: WebClient,
    private val tokenService: TokenService
) {
    private val logger: Logger = LoggerFactory.getLogger(PenClient::class.java)

    fun sendInntektsendring(
        pid: String,
        virk: LocalDate,
        simulertTotalbelopNetto: Int,
        inntektsgrunnlagListe: List<Inntektsgrunnlag>,
        inntektsgrunnlagListeEps: List<Inntektsgrunnlag>?
    ): InnsendingResponse {
        val path = "/api/selvbetjening/inntektsplanleggeren/behandle"
        try {
            return tokenService.getEgressToken(scope = scope, audience = audience, pid = pid, appId = AppId.PEN)
                .let { accessToken ->
                    webClient
                        .post()
                        .uri("$url$path")
                        .header("Authorization", "Bearer $accessToken")
                        .header(NAV_CALL_ID, getCurrentCallId())
                        .accept(MediaType.APPLICATION_JSON)
                        .bodyValue(
                            InnsendingRequest(
                                pid,
                                simulertTotalbelopNetto,
                                virk,
                                inntektsgrunnlagListe,
                                inntektsgrunnlagListeEps?: emptyList(),
                                tokenService.determineLoggedInUser()
                            )
                        )
                        .retrieve()
                        .bodyToMono(InnsendingResponse::class.java)
                        .withMdcContext()
                        .block()
                } ?: throw IllegalStateException("Feilet under sending av inntektsendring til PEN")
        } catch (e: WebClientResponseException) {
            if (HttpStatus.FORBIDDEN == e.statusCode) {
                throw ForbiddenException(AppId.PEN.name, path, e.message, e)
            }
            throw ClientException(AppId.PEN.name, path, e.message, e)
        } catch (e: Exception) {
            throw ClientException(AppId.PEN.name, path, e.message, e)
        }
    }

    fun simulerInntektsendring(
        pid: String,
        virk: LocalDate,
        inntektsgrunnlagListe: List<Inntektsgrunnlag>,
        inntektsgrunnlagListeEps: List<Inntektsgrunnlag>
    ): SimulerEndringUforetrygdResponse {
        val path = "/api/selvbetjening/inntektsplanleggeren/simuler"
        try {
            return tokenService.getEgressToken(scope = scope, audience = audience, pid = pid, appId = AppId.PEN)
                .let { accessToken ->
                    webClient
                        .post()
                        .uri("$url$path")
                        .header("Authorization", "Bearer $accessToken")
                        .header(NAV_CALL_ID, getCurrentCallId())
                        .accept(MediaType.APPLICATION_JSON)
                        .bodyValue(
                            SimuleringEndringUforetrygdRequest(
                                pid,
                                virk,
                                inntektsgrunnlagListe,
                                inntektsgrunnlagListeEps
                            )
                        )
                        .retrieve()
                        .bodyToMono(SimulerEndringUforetrygdResponse::class.java)
                        .withMdcContext()
                        .block()
                } ?: throw IllegalStateException("Feilet under simulering mot PEN")
        } catch (e: WebClientResponseException) {
            if (HttpStatus.FORBIDDEN == e.statusCode) {
                throw ForbiddenException(AppId.PEN.name, path, e.message, e)
            }
            throw ClientException(AppId.PEN.name, path, e.message, e)
        } catch (e: Exception) {
            throw ClientException(AppId.PEN.name, path, e.message, e)
        }
    }

    fun fetchInntektsplanleggerData(pid: String, simuleringFom: LocalDate): Uforetrygd? {
        val path = "/api/selvbetjening/inntektsplanleggeren/data"
        try {
            return tokenService.getEgressToken(scope = scope, audience = audience, pid = pid, appId = AppId.PEN)
                .let { accessToken ->
                    webClient
                        .get()
                        .uri("$url$path?simuleringFom=$simuleringFom")
                        .header("fnr", pid)
                        .header("Authorization", "Bearer $accessToken")
                        .header(NAV_CALL_ID, getCurrentCallId())
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .bodyToMono(Uforetrygd::class.java)
                        .withMdcContext()
                        .block()
                }
        } catch (e: WebClientResponseException) {
            if (HttpStatus.FORBIDDEN == e.statusCode) {
                throw ForbiddenException(AppId.PEN.name, path, e.message, e)
            }
            if (HttpStatus.NOT_FOUND == e.statusCode) {
                return null
            }
            throw ClientException(AppId.PEN.name, path, e.message, e)
        } catch (e: Exception) {
            throw ClientException(AppId.PEN.name, path, e.message, e)
        }
    }

    fun fetchInntektsplanleggerStatus(pid: String, simuleringFom: LocalDate, innsendingsTidspunkt: LocalDateTime): StatusInnsendingResponse? {
        val path = "/api/selvbetjening/inntektsplanleggeren/status"
        val tidspkt = innsendingsTidspunkt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        try {
            return tokenService.getEgressToken(scope = scope, audience = audience, pid = pid, appId = AppId.PEN)
                .let { accessToken ->
                    webClient
                        .get()
                        .uri("$url$path?fom=$tidspkt&endringFom=$simuleringFom")
                        .header("fnr", pid)
                        .header("Authorization", "Bearer $accessToken")
                        .header(NAV_CALL_ID, getCurrentCallId())
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .bodyToMono(StatusInnsendingResponse::class.java)
                        .withMdcContext()
                        .block()
                } ?: throw IllegalStateException("Unable to fetch status from PEN")
        } catch (e: WebClientResponseException) {
            if (HttpStatus.FORBIDDEN == e.statusCode) {
                throw ForbiddenException(AppId.PEN.name, path, e.message, e)
            }
            if (HttpStatus.NOT_FOUND == e.statusCode) {
                return null
            }
            if (HttpStatus.BAD_REQUEST == e.statusCode) {
                logger.warn(e.responseBodyAsString)
            }
            throw ClientException(AppId.PEN.name, path, e.message, e)
        } catch (e: Exception) {
            throw ClientException(AppId.PEN.name, path, e.message, e)
        }
    }
}