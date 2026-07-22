package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.fullmakt

data class ValidRepresentasjonsforholdRequest(
    val representertPid: String,
    val representantPid: String?,
    val validRepresentasjonstyper: List<String>,
    val includeRepresentertNavn: Boolean = false
)