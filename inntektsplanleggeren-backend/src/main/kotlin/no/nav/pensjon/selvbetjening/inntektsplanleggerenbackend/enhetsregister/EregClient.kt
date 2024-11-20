package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.enhetsregister

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.configuration.AppId
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.configuration.CallIdUtil
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.configuration.getCallIdFromMdc
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.enhetsregister.dto.Organisasjon
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.fullmakt.FullmaktClient.Companion.NAV_CALL_ID
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.ClientException
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.ForbiddenException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.time.LocalDate

@Component
class EregClient(
    @Value("\${ereg-services.endpoint.url}") private val url: String,
    private val webClient: WebClient,
) {
    private val logger: Logger = LoggerFactory.getLogger(EregClient::class.java)

    fun hentOrganisasjonsnavn(organisasjonsnummer: String, gyldigDato: LocalDate? = null): String {
        val path = "/api/v2/organisasjon/$organisasjonsnummer/noekkelinfo"
        try {

            return (
                    webClient
                        .get()
                        .uri("$url$path")
                        .header(NAV_CALL_ID, CallIdUtil.getCallIdFromMdc())
                        .header("Nav-Consumer_id", "ufoere")
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .bodyToMono(Organisasjon::class.java)
                        .block())?.navn?.sammensattnavn ?: organisasjonsnummer

        } catch (e: WebClientResponseException) {
            when (e.statusCode) {
                HttpStatus.NOT_FOUND -> {
                    logger.warn("404 fra ereg-services, organisasjon ikke funnet - orgnr: $organisasjonsnummer")
                    return organisasjonsnummer
                }
                HttpStatus.FORBIDDEN -> throw ForbiddenException(AppId.EREG.name, path, e.message, e)
                else -> throw ClientException(AppId.EREG.name, path, e.message, e)
            }
        } catch (e: Exception) {
            throw ClientException(AppId.EREG.name, path, e.message, e)
        }
    }
}