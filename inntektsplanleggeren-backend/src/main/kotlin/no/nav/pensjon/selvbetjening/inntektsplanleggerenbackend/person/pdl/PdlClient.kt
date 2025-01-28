package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.person.pdl

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.configuration.AppId
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.configuration.CallIdUtil
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.configuration.CallIdUtil.Companion.NAV_CALL_ID_NAME
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.configuration.getCallIdFromMdc
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security.AzureAdService
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security.TokenService
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.ClientException
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.ForbiddenException
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.PersonNotFoundException
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Component
class PdlClient(
    @Value("\${pdl.endpoint.url}") private val url: String,
    private val webClient: WebClient,
    @Value("\${pdl.scope}") private val scope: String,
    @Value("\${pdl.audience}") private val audience: String,
    private val tokenService: TokenService,
    private val azureAdService: AzureAdService
) {

    fun performQuery(query: PdlPersonQuery): PdlPerson {
        return performQuery(query, tokenService.getEgressToken(scope, audience, query.variables.ident, AppId.PDL))
    }

    fun performQueryWithElevatedPriveleges(query: PdlPersonQuery): PdlPerson {
        return performQuery(query, azureAdService.retrieveClientCredentialsToken(listOf(scope)))
    }

    private fun performQuery(query: PdlPersonQuery, token: String?): PdlPerson {
        val response = token.let {
            webClient
                .post()
                .uri(url)
                .header("Authorization", "Bearer $it")
                .header(NAV_CALL_ID_NAME, CallIdUtil.getCallIdFromMdc())
                .header(PDL_BEHANDLINGSNUMMER_KEY, PDL_BEHANDLINGSNUMMER_VALUE)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(query)
                .retrieve()
                .bodyToMono(HentPersonResponse::class.java)
                .block()
        }

        val person = response?.data?.hentPdlPerson

        return if (person != null) {
            person
        } else {
            response?.errors.takeIf { !it.isNullOrEmpty() }?.let { errors ->
                val error = errors.first()
                logger.error("Kallet feilet mot PDL: kode ${error.extensions?.code}")
                when (error.extensions?.code) {
                    PdlErrorCodes.UNAUTHENTICATED -> throw ForbiddenException(
                        AppId.PDL.name,
                        PDL_API,
                        error.message,
                        null
                    )

                    PdlErrorCodes.UNAUTHORIZED -> throw ForbiddenException(
                        AppId.PDL.name,
                        PDL_API,
                        error.message,
                        null
                    )

                    PdlErrorCodes.NOT_FOUND -> throw PersonNotFoundException(AppId.PDL.name, PDL_API, error.message, null)
                    PdlErrorCodes.BAD_REQUEST -> throw ClientException(AppId.PDL.name, PDL_API, error.message, null)
                    PdlErrorCodes.SERVER_ERROR -> throw ClientException(AppId.PDL.name, PDL_API, error.message, null)
                    else -> throw ClientException(AppId.PDL.name, PDL_API, error.message, null)
                }
            }
            throw ClientException(AppId.PDL.name, PDL_API, "Failed calling PDL", null)
        }

    }

    companion object {
        const val PDL_BEHANDLINGSNUMMER_VALUE = "B255"
        const val PDL_BEHANDLINGSNUMMER_KEY = "Behandlingsnummer"
        const val PDL_API = "pdl-api"

        private val logger: Logger = LoggerFactory.getLogger(PdlClient::class.java)
    }
}