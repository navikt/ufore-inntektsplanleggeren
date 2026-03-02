package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.person.parallellesannheter

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.configuration.retryOnTimeout
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.configuration.withMdcContext
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.person.parallellesannheter.dto.AdressebeskyttelseParallelleSannheterContainer
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.person.parallellesannheter.dto.FoedselsdatoParallelleSannheterContainer
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.person.parallellesannheter.dto.NavnParallelleSannheterContainer
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.ClientException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException

@Component
class ParallelleSannheterClient(private val webClient: WebClient,
                                @Value("\${parallellesannheter.endpoint.url}") private val url: String) {

    private val logger: Logger = LoggerFactory.getLogger(ParallelleSannheterClient::class.java)

    fun decideFoedselsdato(foedselsdatoSannheter: FoedselsdatoParallelleSannheterContainer): FoedselsdatoParallelleSannheterContainer {
        val path = "/api/foedselsdato"
        try {
            return webClient
                .post()
                .uri("$url$path")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(foedselsdatoSannheter)
                .retrieve()
                .bodyToMono(FoedselsdatoParallelleSannheterContainer::class.java)
                .retryWhen(retryOnTimeout)
                .withMdcContext()
                .block()
                ?.lockDecision()?: FoedselsdatoParallelleSannheterContainer(null)
        } catch (e: WebClientResponseException) {
            handleErrorResponse(e, path)
        } catch (e: Exception) {
            handleUnexpectedError(e, path)
        }

        return FoedselsdatoParallelleSannheterContainer(null)
    }

    fun decideNavn(navnSannheter: NavnParallelleSannheterContainer): NavnParallelleSannheterContainer {
        val path = "/api/navn"
        try {
            return webClient
                .post()
                .uri("$url$path")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(navnSannheter)
                .retrieve()
                .bodyToMono(NavnParallelleSannheterContainer::class.java)
                .retryWhen(retryOnTimeout)
                .withMdcContext()
                .block()
                ?.lockDecision()?: NavnParallelleSannheterContainer(null)
        } catch (e: WebClientResponseException) {
            handleErrorResponse(e, path)
        } catch (e: Exception) {
            handleUnexpectedError(e, path)
        }

        return NavnParallelleSannheterContainer(null)
    }

    fun decideAdressebeskyttelse(adressebeskyttelseSannheter: AdressebeskyttelseParallelleSannheterContainer): AdressebeskyttelseParallelleSannheterContainer {
        val path = "/api/adressebeskyttelse"
        try {
            return webClient
                .post()
                .uri("$url$path")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(adressebeskyttelseSannheter)
                .retrieve()
                .bodyToMono(AdressebeskyttelseParallelleSannheterContainer::class.java)
                .retryWhen(retryOnTimeout)
                .withMdcContext()
                .block()
                ?.lockDecision()?: AdressebeskyttelseParallelleSannheterContainer(null)
        } catch (e: WebClientResponseException) {
            handleErrorResponse(e, path)
        } catch (e: Exception) {
            handleUnexpectedError(e, path)
        }

        return AdressebeskyttelseParallelleSannheterContainer(null)
    }




    private fun handleErrorResponse(e: WebClientResponseException, service: String) {
        val message: String
        if (HttpStatus.NOT_IMPLEMENTED == e.statusCode) {
            message = "Not able to decide parallell sannhet: ${e.responseBodyAsString}"
            logger.error(message, e)
        } else {
            message = "Unexpected error when deciding parallell sannhet: ${e.responseBodyAsString}"
            logger.error(message, e)
        }
        throw ClientException(APP_ID, service, message, e)
    }

    private fun handleUnexpectedError(e: Exception, service: String) {
        val message = "Unexpected error when deciding parallell sannhet"
        logger.error(message, e)
        throw ClientException(APP_ID, service, message, e)
    }

    companion object{
        private const val APP_ID = "parallelle-sannheter"
    }


}