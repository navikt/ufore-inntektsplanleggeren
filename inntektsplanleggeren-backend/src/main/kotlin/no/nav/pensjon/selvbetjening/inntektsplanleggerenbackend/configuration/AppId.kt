package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.configuration

enum class AppId(
    val supportsTokenX: Boolean,
    val supportsFullmakt: Boolean,
    val useAzureOBo: Boolean
) {
    PENSJON_FULLMAKT(true, false, false),
    SKJERMING(false, false, false),
    PEN(true, false, true),
    PDL(true, false, true),
    INNTEKTSKOMPONENTEN(false, false, false),
    EREG(false, false, false)
}