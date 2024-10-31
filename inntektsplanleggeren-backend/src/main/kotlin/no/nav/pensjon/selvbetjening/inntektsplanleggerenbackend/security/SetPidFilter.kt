package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.fullmakt.FullmaktClient
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.fullmakt.FullmaktException
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.fullmakt.FullmaktsforholdDto
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.person.PersonService
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.skjerming.SkjermingClient
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.util.Masker
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.server.ResponseStatusException

@Component
class SetPidFilter(
    private val fullmaktClient: FullmaktClient,
    private val tokenService: TokenService,
    private val skjermingClient: SkjermingClient,
    private val personService: PersonService
): OncePerRequestFilter() {

    private val log: Logger = LoggerFactory.getLogger(SetPidFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        //log.debug("Setter Pid på context")
        val authHeader = request.getHeader("Authorization")

        if (authHeader != null) {
            val authenticatedUserDetails: AuthenticatedUserDetails
            if (tokenService.determineTokenType() == TokenService.TokenType.TOKEN_X) {
                log.info("Borger context")
                val requestingPid = tokenService.determineRequestingPid()

                val navOnBehalfOfCookie = request.cookies?.firstOrNull{ cookie -> cookie.name.equals("nav-obo") }
                authenticatedUserDetails = if (navOnBehalfOfCookie != null) {
                    log.info("Cookie'en nav-obo er satt og det antyder fullmaktscenario")
                    val fullmaktsgiverPid = navOnBehalfOfCookie.value
                    if (requestingPid != "" && requestingPid != fullmaktsgiverPid) {
                        val fullmaktsforhold = haandterFullmakt(fullmaktsgiverPid, requestingPid)
                        AuthenticatedUserDetails(fullmaktsgiverPid, fullmaktsforhold.fullmaktsgiverNavn)
                    } else {
                        AuthenticatedUserDetails(requestingPid, null)
                    }
                } else {
                    AuthenticatedUserDetails(requestingPid, null)
                }

            } else {
                val pid = request.getHeader("pid")
                    ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Pid not specified!")
                checkVeilederAuthorization(pid)
                log.info("Veileder on behalf of ${Masker.maskPid(pid)}")
                authenticatedUserDetails = AuthenticatedUserDetails(pid, null)
            }
            (SecurityContextHolder.getContext().authentication as JwtAuthenticationToken).details = authenticatedUserDetails
        }
        filterChain.doFilter(request, response)
    }

    private fun haandterFullmakt(fullmaktsgiverPid: String, requestingPid: String): FullmaktsforholdDto {
        try {
            val harGyldigFullmakt = fullmaktClient.hasValidRepresentasjonsforhold(fullmaktsgiverPid, requestingPid)
            if (harGyldigFullmakt == null || !harGyldigFullmakt.hasValidRepresentasjonsforhold) {
                log.info("Fullmaktsforhold er ikke funnet. Nekter adgang")
                throw ResponseStatusException(HttpStatus.FORBIDDEN)
            }
            return harGyldigFullmakt

        } catch (e: FullmaktException) {
            log.error("Noe gikk galt ved kall til fullmakt. Nekter adgang")
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        }
    }

    private fun checkVeilederAuthorization(pid: String) {
        if (!tokenService.isUserInSkjermetGroup() && skjermingClient.isSkjermet(pid)) {
            log.info("Bruker skjermet, saksbehandler mangler autorisering. Nekter tilgang.")
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        }
        if(!personService.hasSaksbehandlerAccessToPid(pid)){
            log.info("Bruker adressebeskyttet, saksbehandler mangler autorisering. Nekter tilgang.")
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        }
    }
}