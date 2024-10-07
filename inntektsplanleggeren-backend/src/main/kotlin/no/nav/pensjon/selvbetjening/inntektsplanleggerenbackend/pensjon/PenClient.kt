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

    fun simulerInntektsendring(
        pid: String,
        virk: LocalDate?,
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
                        .bodyValue(SimuleringEndringUforetrygdRequest(virk, inntektsgrunnlagListe, inntektsgrunnlagListeEps))
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

    fun fetchInntektsplanleggerData(pid: String): Pensjonsdata? {
        return Pensjonsdata(
            20000,
            43093.0,
            500434,
            true,
            false,
            true,
            false,
            true,
            false,
            true,
            null,
            null,
            null
        )
    }

    //TODO: Should be removed
    fun fetchInitialInntektsplanleggerPensjonsdata(pid: String): InitialInntektsplanleggerPensjonsdata? {
        val path = "/pen/api/selvbetjening/inntektsplanleggeren/initial"
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
                        .bodyToMono(InitialInntektsplanleggerPensjonsdata::class.java)
                        .block()
                } ?: throw IllegalStateException("Unable to fetch initial pensjonsdata from PEN")
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