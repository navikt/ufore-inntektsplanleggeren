package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.dto

enum class Inntektshendelse(val code: String) {
    VARSLET("Varslet"),
    REGISTRERT("Registrert"),
    BENYTTET("Benyttet");

    companion object {
        fun getHendelseForCode(code: String?): Inntektshendelse? =
            Inntektshendelse.entries.firstOrNull { it.code == code }
    }
}