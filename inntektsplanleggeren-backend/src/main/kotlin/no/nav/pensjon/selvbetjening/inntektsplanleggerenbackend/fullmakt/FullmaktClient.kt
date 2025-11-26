package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.fullmakt


import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.configuration.AppId
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
class FullmaktClient(
    @Value("\${fullmakt.endpoint.url}") private val baseUrl: String,
    @Value("\${fullmakt.scope}") private val scope: String,
    @Value("\${fullmakt.audience}") private val audience: String,
    private val webClient: WebClient,
    private val tokenService: TokenService
) {

    fun hasValidRepresentasjonsforhold(httpMethod: String, fullmaktsgiverPid: String, fullmektigPid: String): RepresentasjonsforholdValidity? {
        return try {
            tokenService.getEgressToken(scope, audience, fullmektigPid, AppId.PENSJON_FULLMAKT).let {
                webClient
                    .get()
                    .uri(urlValidRepresentasjonsforhold(httpMethod))
                    .headers { headers: HttpHeaders ->
                        headers.setBearerAuth(it!!)
                        headers[HttpHeaders.CONTENT_TYPE] = MediaType.APPLICATION_JSON_VALUE
                        headers[HttpHeaders.ACCEPT] = MediaType.APPLICATION_JSON_VALUE
                        headers[NAV_CALL_ID] = MDC.get(NAV_CALL_ID)
                        headers[FULLMAKTSGIVER_PID] = fullmaktsgiverPid
                    }
                    .retrieve()
                    .bodyToMono(RepresentasjonsforholdValidity::class.java)
                    .block()
            }

        } catch (e: WebClientResponseException) {
            logger.error("Kall til fullmaktstjenesten feilet med melding: ${e.responseBodyAsString}", e)
            throw FullmaktException(SERVICE, "hasValidRepresentasjonsforhold", "Failed to call service: " + e.responseBodyAsString, e)
        } catch (e: RuntimeException) { // e.g. when connection broken
            logger.error("Kall til fullmaktstjenesten feilet: ${e.message}")
            throw FullmaktException(SERVICE, "hasValidRepresentasjonsforhold", "Failed to call service", e)
        }
    }

    private fun urlValidRepresentasjonsforhold(httpMethod: String): String {
        val representasjonstyperBasertPaaHttpMethode = if (listOf("POST", "PUT", "DELETE").contains(httpMethod))
            VALID_SKRIV_REPRESENTASJONSTYPER
        else
            VALID_LES_REPRESENTASJONSTYPER

        return UriComponentsBuilder.fromUriString(baseUrl)
            .path(PATH_HASREPRESENTASJONSFORHOLD)
            .queryParam(VALID_REPRESENTASJONSTYPER_KEY, representasjonstyperBasertPaaHttpMethode)
            .queryParam(INCLUDE_NAVN_KEY, false)
            .build()
            .toUriString()
    }

    companion object {
        private const val SERVICE = "Fullmakt"
        private const val PATH_HASREPRESENTASJONSFORHOLD = "/representasjon/hasValidRepresentasjonsforhold"

        const val NAV_CALL_ID = "Nav-Call-Id"
        const val FULLMAKTSGIVER_PID = "fullmaktsgiverPid"
        const val INCLUDE_NAVN_KEY = "includeFullmaktsgiverNavn"
        const val VALID_REPRESENTASJONSTYPER_KEY = "validRepresentasjonstyper"
        private val VALID_LES_REPRESENTASJONSTYPER = setOf(
            "UFORETRYGD_LES", "PENSJON_BEGRENSET")

        private val VALID_SKRIV_REPRESENTASJONSTYPER = setOf(
            "UFORETRYGD_SKRIV", "PENSJON_FULLSTENDIG")

        private val logger: Logger = LoggerFactory.getLogger(FullmaktClient::class.java)

    }
}

