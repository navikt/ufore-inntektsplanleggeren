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
    private val authorizationService: AuthorizationService,
    private val pidEncryptionClient: PidEncryptionClient
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
                is NoFullmaktPresentException -> response.errorResponse(ErrorCode.NO_FULLMAKT_PRESENT, path, HttpStatus.FORBIDDEN)
                is LoginLevelTooLowException -> response.errorResponse(ErrorCode.LOGIN_LEVEL_TOO_LOW, path, HttpStatus.FORBIDDEN)
                is VeilederUnauthorizedException -> response.errorResponse(ErrorCode.VEILEDER_UNAUTHORIZED, path, HttpStatus.FORBIDDEN)
                is ResponseStatusException -> response.errorResponse(ErrorCode.NO_PID_PRESENT, path, HttpStatus.BAD_REQUEST)
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

                authenticatedUserDetails = authorizationService.checkBorgerTilgang(request.method, navOnBehalfOfCookie)
            } else {
                val pidFromHeader = request.getHeader("pid")
                    ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Pid not specified!")
                val pid = if (isEncryptedPid(pidFromHeader)) {
                    logger.info("Pid is encrypted. Decrypting...")
                    pidEncryptionClient.decrypt(pidFromHeader)!!
                } else {
                    logger.warn("Using unencrypted PID from request")
                    pidFromHeader
                }
                log.info("Veileder on behalf of ${Masker.maskPid(pid)}")
                authorizationService.checkVeilederTilgangTilInnbygger(pid)
                authenticatedUserDetails = AuthenticatedUserDetails(pid, false)
            }
            (SecurityContextHolder.getContext().authentication as JwtAuthenticationToken).details =
                authenticatedUserDetails
        }
        filterChain.doFilter(request, response)
    }

    private fun isEncryptedPid(pid: String): Boolean = pid.contains('.')

    private fun HttpServletResponse.errorResponse(
        error: ErrorCode,
        path: String,
        status: HttpStatus,
    ) {
        val errorResponse = SetPidFilterErrorResponse(
            timestamp = LocalDateTime.now().toString(),
            status = status.value(),
            error = status.name,
            message = error,
            path = path
        )
        this.apply {
            this.status = status.value()
            this.setHeader("Content-Type", "application/json")
            this.writer.write(ObjectMapper().writeValueAsString(errorResponse))
        }
    }

}
