package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
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
    private val tokenService: TokenService,
    private val authorizationService: AuthorizationService
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
                val navOnBehalfOfCookie = request.cookies?.firstOrNull { cookie -> cookie.name.equals("nav-obo") }
                authenticatedUserDetails = authorizationService.checkBorgerTilgang(navOnBehalfOfCookie)
            } else {
                val pid = request.getHeader("pid")
                    ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Pid not specified!")
                log.info("Veileder on behalf of ${Masker.maskPid(pid)}")
                authorizationService.checkVeilederTilgangTilInnbygger(pid)
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

}
