package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security

data class AuthenticatedUserDetails(
    val pid: String,
    val isFullmakt: Boolean
)