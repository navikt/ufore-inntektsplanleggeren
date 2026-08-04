package no.nav.ufore.inntektsplanleggeren.security

data class AuthenticatedUserDetails(
    val pid: String,
    val isFullmakt: Boolean
)