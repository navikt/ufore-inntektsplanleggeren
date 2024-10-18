package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.configuration.AppId
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.configuration.CallIdUtil
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.configuration.getCallIdFromMdc
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.fullmakt.FullmaktClient.Companion.NAV_CALL_ID
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.ClientException
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.ForbiddenException
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.*
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security.TokenService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.time.LocalDate

@Component
class PenClient(
    @Value("\${pen.endpoint.url}") private val url: String,
    @Value("\${pen.scope}") private val scope: String,
    @Value("\${pen.audience}") private val audience: String,
    private val webClient: WebClient,
    private val tokenService: TokenService
) {

    fun sendInntektsendring(
        pid: String,
        virk: LocalDate,
        simulertTotalbelopNetto: Int,
        inntektsgrunnlagListe: List<Inntektsgrunnlag>,
        inntektsgrunnlagListeEps: List<Inntektsgrunnlag>?
    ): InnsendingResponse {
        val path = "/pen/api/selvbetjening/inntektsplanleggeren/behandle"
        try {
            return tokenService.getEgressToken(scope = scope, audience = audience, pid = pid, appId = AppId.PEN)
                .let { accessToken ->
                    webClient
                        .post()
                        .uri("$url$path")
                        .header("Authorization", "Bearer $accessToken")
                        .header(NAV_CALL_ID, CallIdUtil.getCallIdFromMdc())
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
                        .block()
                } ?: throw IllegalStateException("Unable to fetch initial pensjonsdata from PEN")
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
        val path = "/pen/api/selvbetjening/inntektsplanleggeren/simuler"
        try {
            return tokenService.getEgressToken(scope = scope, audience = audience, pid = pid, appId = AppId.PEN)
                .let { accessToken ->
                    webClient
                        .post()
                        .uri("$url$path")
                        .header("Authorization", "Bearer $accessToken")
                        .header(NAV_CALL_ID, CallIdUtil.getCallIdFromMdc())
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
                        .block()
                } ?: throw IllegalStateException("Unable to fetch initial pensjonsdata from PEN")
        } catch (e: WebClientResponseException) {
            if (HttpStatus.FORBIDDEN == e.statusCode) {
                throw ForbiddenException(AppId.PEN.name, path, e.message, e)
            }
            throw ClientException(AppId.PEN.name, path, e.message, e)
        } catch (e: Exception) {
            throw ClientException(AppId.PEN.name, path, e.message, e)
        }
    }

    fun fetchInntektsplanleggerData(pid: String, simuleringFom: LocalDate): Pensjonsdata? {
        val path = "/pen/api/selvbetjening/inntektsplanleggeren/data"
        try {
            return tokenService.getEgressToken(scope = scope, audience = audience, pid = pid, appId = AppId.PEN)
                .let { accessToken ->
                    webClient
                        .get()
                        .uri("$url$path?simuleringFom=$simuleringFom")
                        .header("fnr", pid)
                        .header("Authorization", "Bearer $accessToken")
                        .header(NAV_CALL_ID, CallIdUtil.getCallIdFromMdc())
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .bodyToMono(Pensjonsdata::class.java)
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
}