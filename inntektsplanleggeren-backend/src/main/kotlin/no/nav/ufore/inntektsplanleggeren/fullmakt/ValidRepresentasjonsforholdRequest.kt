package no.nav.ufore.inntektsplanleggeren.fullmakt

data class ValidRepresentasjonsforholdRequest(
    val representertPid: String,
    val representantPid: String?,
    val validRepresentasjonstyper: List<String>,
    val includeRepresentertNavn: Boolean = false
)