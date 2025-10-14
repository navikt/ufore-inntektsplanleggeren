package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.logging

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.util.NAV_CALL_ID
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.util.NAV_CALL_ID_MDC
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.util.Masker
import org.slf4j.MDC
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
class MdcFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        MDC.put(NAV_CALL_ID_MDC, request.getHeader(NAV_CALL_ID) ?: UUID.randomUUID().toString())

        if (SecurityContextHolder.getContext().authentication is JwtAuthenticationToken) {
            (SecurityContextHolder.getContext().authentication as JwtAuthenticationToken).token.let { token ->
                token.getClaim<String>("NAVident")
                    ?.let { saksbehandler ->
                        MDC.put("nav_ident", saksbehandler)
                    }
                token.getClaim<String>("pid")
                    ?.let { pid ->
                        MDC.put("pid", Masker.maskPid(pid))
                    }
            }
        }

        filterChain.doFilter(request, response)
    }
}