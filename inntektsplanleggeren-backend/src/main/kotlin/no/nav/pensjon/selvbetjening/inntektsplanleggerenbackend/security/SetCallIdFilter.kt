package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.configuration.NAV_CALL_ID
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
class SetCallIdFilter: OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        MDC.put(NAV_CALL_ID, request.getHeader(NAV_CALL_ID) ?: UUID.randomUUID().toString())
        filterChain.doFilter(request, response)
    }
}