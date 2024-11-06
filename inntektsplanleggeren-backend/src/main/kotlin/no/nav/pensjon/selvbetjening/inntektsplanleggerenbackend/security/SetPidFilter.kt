package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.fullmakt.FullmaktClient
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.fullmakt.FullmaktException
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.fullmakt.RepresentasjonsforholdValidity
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
import java.time.LocalDateTime

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
        try {
            setAuthenticatedUserDetails(request, filterChain, response)
        } catch (e: Exception) {
            val path = request.requestURI
            when (e) {
                is NoFullmaktPresentException -> forbiddenResponse(response, ErrorCode.NO_FULLMAKT_PRESENT, path)
                is LoginLevelTooLowException -> forbiddenResponse(response, ErrorCode.LOGIN_LEVEL_TOO_LOW, path)
                is VeilederUnauthorizedException -> forbiddenResponse(response, ErrorCode.VEILEDER_UNAUTHORIZED, path)
                else -> throw e
            }
        }
    }

    private fun setAuthenticatedUserDetails(
        request: HttpServletRequest,
        filterChain: FilterChain,
        response: HttpServletResponse,
    ) {
        val authHeader = request.getHeader("Authorization")

        if (authHeader != null) {
            val authenticatedUserDetails: AuthenticatedUserDetails
            if (tokenService.determineTokenType() == TokenService.TokenType.TOKEN_X) {
                log.info("Borger context")
                val requestingPid = tokenService.determineRequestingPid()

                val navOnBehalfOfCookie = request.cookies?.firstOrNull { cookie -> cookie.name.equals("nav-obo") }
                authenticatedUserDetails = if (navOnBehalfOfCookie != null) {
                    log.info("Cookie'en nav-obo er satt og det antyder fullmaktscenario")
                    val fullmaktsgiverPid = navOnBehalfOfCookie.value
                    if (requestingPid != "" && requestingPid != fullmaktsgiverPid) {
                        haandterFullmakt(fullmaktsgiverPid, requestingPid)
                        AuthenticatedUserDetails(
                            fullmaktsgiverPid, true
                        )
                    } else {
                        checkAdressebeskyttelseAndLoginLevel(requestingPid)
                        AuthenticatedUserDetails(requestingPid, false)
                    }
                } else {
                    checkAdressebeskyttelseAndLoginLevel(requestingPid)
                    AuthenticatedUserDetails(requestingPid, false)
                }

            } else {
                val pid = request.getHeader("pid")
                    ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Pid not specified!")
                checkVeilederAuthorization(pid)
                log.info("Veileder on behalf of ${Masker.maskPid(pid)}")
                authenticatedUserDetails = AuthenticatedUserDetails(pid, false)
            }
            (SecurityContextHolder.getContext().authentication as JwtAuthenticationToken).details =
                authenticatedUserDetails
        }
        filterChain.doFilter(request, response)
    }

    private fun forbiddenResponse(response: HttpServletResponse, errorCode: ErrorCode, path: String
    ) {
        val forbiddenStatus = HttpStatus.FORBIDDEN.value()
        val mapper = ObjectMapper()
        val errorResponse = SetPidFilterErrorResponse(
            timestamp = LocalDateTime.now().toString(),
            status = forbiddenStatus,
            error = HttpStatus.FORBIDDEN.name,
            message = errorCode,
            path = path
        )
        response.apply {
            status = forbiddenStatus
            setHeader("Content-Type", "application/json")
            writer.write(mapper.writeValueAsString(errorResponse))
        }
    }

    private fun checkAdressebeskyttelseAndLoginLevel(requestingPid: String) {
            if (!tokenService.isLoginLevelHigh() && personService.hasAdressebeskyttelse(requestingPid)) {
                log.info("Bruker adressebeskyttet, innloggingsnivå for lavt. Nekter adgang")
                throw LoginLevelTooLowException()
        }
    }

    private fun haandterFullmakt(fullmaktsgiverPid: String, requestingPid: String): RepresentasjonsforholdValidity {
        try {
            val harGyldigFullmakt = fullmaktClient.hasValidRepresentasjonsforhold(fullmaktsgiverPid, requestingPid)
            if (harGyldigFullmakt == null || !harGyldigFullmakt.hasValidRepresentasjonsforhold) {
                log.info("Fullmaktsforhold er ikke funnet. Nekter adgang")
                throw NoFullmaktPresentException()
            }

            if(personService.hasAdressebeskyttelse(fullmaktsgiverPid)) {
                log.info("Fullmaktsforhold for bruker med diskresjon. Nekter adgang")
                throw NoFullmaktPresentException()
            }

            return harGyldigFullmakt
        } catch (e: FullmaktException) {
            log.error("Noe gikk galt ved kall til fullmakt. Nekter adgang")
            log.warn("FullmaktException: ${e.message}")
            throw NoFullmaktPresentException()
        }
    }

    private fun checkVeilederAuthorization(pid: String) {
        if (!tokenService.isUserInSkjermetGroup() && skjermingClient.isSkjermet(pid)) {
            log.info("Bruker skjermet, saksbehandler mangler autorisering. Nekter tilgang.")
            throw VeilederUnauthorizedException()
        }
        if (!personService.hasSaksbehandlerAccessToPid(pid)){
            log.info("Bruker adressebeskyttet, saksbehandler mangler autorisering. Nekter tilgang.")
            throw VeilederUnauthorizedException()
        }
    }
}
