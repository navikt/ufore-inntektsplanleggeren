package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security

import org.springframework.security.core.context.SecurityContextHolder

class SecurityContextUtil {
    companion object {
        fun getPidFromContext(): String =
            (SecurityContextHolder.getContext().authentication.details as AuthenticatedUserDetails).pid

        fun getFullmaktsgiverNavn(): String? =
            (SecurityContextHolder.getContext().authentication.details as AuthenticatedUserDetails).fullmaktsgiverNavn
    }
}