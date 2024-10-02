package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto

import java.time.LocalDate


data class Inntektsgrunnlag(
    val inntektsgrunnlagId: Long? = null,
    val fomDato: LocalDate? = null,
    val tomDato: LocalDate? = null,
    val endringstidspunkt: LocalDate? = null,
    val belop: Int? = 0,
    val bruk: Boolean = false,
    val kopiertFraGammeltKrav: Boolean = false,
    val registerOpprettetAv: String? = null,
    val grunnlagKilde: String? = null,
    val registerKilde: String? = null,
    val inntektType: String? = null,
    val inntektHendelseType: String? = null,
    val grunnIkkeReduksjonType: String? = null,
    val persongrunnlagId: Long? = null,
    val changeStamp: String? = null,
    val version: Int? = null
)

enum class InntektsgrunnlagType(val code: String) {
    ARBEIDSINNTEKT("FORINTARB"),
    NAERINGSINNTEKT(""),
    INNTEKT_UTLAND(""),
    PENSJON_UTLAND(""),
    ANDRE_YTELSER("")
}