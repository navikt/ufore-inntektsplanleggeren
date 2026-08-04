package no.nav.ufore.inntektsplanleggeren.logging

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import no.nav.ufore.inntektsplanleggeren.security.TokenService
import no.nav.ufore.inntektsplanleggeren.util.LOGGED_IN_PID
import no.nav.ufore.inntektsplanleggeren.util.NAV_CALL_ID_HEADER
import no.nav.ufore.inntektsplanleggeren.util.Masker
import no.nav.ufore.inntektsplanleggeren.util.NAV_CALL_ID_MDC
import no.nav.ufore.inntektsplanleggeren.util.NAV_IDENT
import no.nav.ufore.inntektsplanleggeren.util.OBO_PID
import org.slf4j.MDC
import org.springframework.core.annotation.Order
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
@Order(1)
class MdcFilter(val tokenService: TokenService) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            MDC.put(NAV_CALL_ID_MDC, request.getHeader(NAV_CALL_ID_HEADER) ?: UUID.randomUUID().toString())
            if(SecurityContextHolder.getContext().authentication is JwtAuthenticationToken) {
                if (tokenService.isUserLoggedInAsSaksbehandler()) {
                    MDC.put(NAV_IDENT, tokenService.determineLoggedInUserId())
                }
                if (tokenService.isUserLoggedInAsPerson()) {
                    MDC.put(LOGGED_IN_PID, Masker.maskPid(tokenService.determineLoggedInUserId()))
                }
            }
            request.cookies?.firstOrNull { cookie -> cookie.name.equals("nav-obo") }?.let { cookie ->
                MDC.put(OBO_PID, Masker.maskPid(cookie.value))
            }

            filterChain.doFilter(request, response)
        } finally {
            MDC.clear()
        }
    }
}