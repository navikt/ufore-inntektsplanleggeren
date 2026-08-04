package no.nav.ufore.inntektsplanleggeren.inntekt.dto

enum class Inntektshendelse(val code: String) {
    VARSLET("Varslet"),
    REGISTRERT("Registrert"),
    IKKE_REGISTRERT("Ikke Registrert"),
    BENYTTET("Benyttet");

    companion object {
        fun getHendelseForCode(code: String?): Inntektshendelse? =
            Inntektshendelse.entries.firstOrNull { it.code == code }
    }
}