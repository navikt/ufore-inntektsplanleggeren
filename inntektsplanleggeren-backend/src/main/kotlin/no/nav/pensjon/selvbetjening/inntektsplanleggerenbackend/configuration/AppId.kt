package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.configuration

enum class AppId(
    val supportsTokenX: Boolean,
    val supportsFullmakt: Boolean
) {
    PENSJON_FULLMAKT(true, false),
    SKJERMING(false, false),
    PEN(true, false),
    PDL(true, false),
    INNTEKTSKOMPONENTEN(false, false),
    EREG(false, false)
}