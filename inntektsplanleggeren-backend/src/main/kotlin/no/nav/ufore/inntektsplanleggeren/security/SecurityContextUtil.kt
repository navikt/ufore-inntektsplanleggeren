package no.nav.ufore.inntektsplanleggeren.security

import org.springframework.security.core.context.SecurityContextHolder

class SecurityContextUtil {
    companion object {
        fun getPidFromContext(): String =
            (SecurityContextHolder.getContext().authentication!!.details as AuthenticatedUserDetails).pid

        fun isFullmakt(): Boolean =
            (SecurityContextHolder.getContext().authentication!!.details as AuthenticatedUserDetails).isFullmakt
    }
}