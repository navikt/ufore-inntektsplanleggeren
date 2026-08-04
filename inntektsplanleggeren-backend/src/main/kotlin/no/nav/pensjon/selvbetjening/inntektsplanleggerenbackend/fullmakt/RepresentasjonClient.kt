package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.fullmakt


import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.configuration.AppId
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.configuration.retryOnTimeout
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.configuration.withMdcContext
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security.TokenService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.util.UriComponentsBuilder

@Component
class RepresentasjonClient(
    @Value("\${fullmakt.endpoint.url}") private val baseUrl: String,
    @Value("\${fullmakt.scope}") private val scope: String,
    @Value("\${fullmakt.audience}") private val audience: String,
    private val webClient: WebClient,
    private val tokenService: TokenService
) {

    fun hasValidRepresentasjonsforhold(httpMethod: String, representertPid: String, representantPid: String): RepresentasjonsforholdValidity? {
        return try {
            tokenService.getEgressToken(scope, audience, representantPid, AppId.PENSJON_REPRESENTASJON).let {
                webClient
                    .post()
                    .uri(urlValidRepresentasjonsforhold())
                    .bodyValue(ValidRepresentasjonsforholdRequest(
                        representertPid,
                        representantPid,
                        requiredRepresentasjonstyper(httpMethod)))
                    .headers { headers: HttpHeaders ->
                        headers.setBearerAuth(it!!)
                        headers[HttpHeaders.CONTENT_TYPE] = MediaType.APPLICATION_JSON_VALUE
                        headers[HttpHeaders.ACCEPT] = MediaType.APPLICATION_JSON_VALUE
                        headers[NAV_CALL_ID] = MDC.get(NAV_CALL_ID)
                    }
                    .retrieve()
                    .bodyToMono(RepresentasjonsforholdValidity::class.java)
                    .retryWhen(retryOnTimeout)
                    .withMdcContext()
                    .block()
            }

        } catch (e: WebClientResponseException) {
            logger.error("Kall til representasjonstjenesten feilet med melding: ${e.responseBodyAsString}", e)
            throw FullmaktException(SERVICE, "hasValidRepresentasjonsforhold", "Failed to call service: " + e.responseBodyAsString, e)
        } catch (e: RuntimeException) { // e.g. when connection broken
            logger.error("Kall til representasjonstjenesten feilet: ${e.message}")
            throw FullmaktException(SERVICE, "hasValidRepresentasjonsforhold", "Failed to call service", e)
        }
    }

    private fun urlValidRepresentasjonsforhold() =
        UriComponentsBuilder.fromUriString(baseUrl)
            .path(PATH_HASREPRESENTASJONSFORHOLD)
            .build()
            .toUriString()


    companion object {
        private const val SERVICE = "Representasjon"
        private const val PATH_HASREPRESENTASJONSFORHOLD = "/representasjon/hasValidRepresentasjonsforhold"

        const val NAV_CALL_ID = "Nav-Call-Id"
        private val logger: Logger = LoggerFactory.getLogger(RepresentasjonClient::class.java)

    }
}

fun requiredRepresentasjonstyper(httpMethod: String) =
    if (listOf("POST", "PUT", "DELETE").contains(httpMethod))
        VALID_SKRIV_REPRESENTASJONSTYPER
    else
        VALID_LES_REPRESENTASJONSTYPER

private val VALID_LES_REPRESENTASJONSTYPER = listOf(
    "UFORETRYGD_LES", "VERGE_UFORETRYGD_LES")

private val VALID_SKRIV_REPRESENTASJONSTYPER = listOf(
    "UFORETRYGD_SKRIV", "VERGE_UFORETRYGD_SKRIV")
