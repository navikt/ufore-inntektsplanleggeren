package no.nav.ufore.inntektsplanleggeren.pensjon.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime


data class Inntektsgrunnlag(
    @JsonProperty("inntektsgrunnlagId")
    val inntektsgrunnlagId: Long? = null,
    @JsonProperty("fomDato")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    val fomDato: OffsetDateTime? = null,
    @JsonProperty("tomDato")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    val tomDato: OffsetDateTime? = null,
    @JsonProperty("endringstidspunkt")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    val endringstidspunkt: OffsetDateTime? = null,
    @JsonProperty("belop")
    val belop: Int? = 0,
    @JsonProperty("bruk")
    val bruk: Boolean = false,
    @JsonProperty("kopiertFraGammeltKrav")
    val kopiertFraGammeltKrav: Boolean = false,
    @JsonProperty("registerOpprettetAv")
    val registerOpprettetAv: String? = null,
    @JsonProperty("grunnlagKilde")
    val grunnlagKilde: String? = null,
    @JsonProperty("registerKilde")
    val registerKilde: String? = null,
    @JsonProperty("inntektType")
    val inntektType: String? = null,
    @JsonProperty("inntektHendelseType")
    val inntektHendelseType: String? = null,
    @JsonProperty("grunnIkkeReduksjonType")
    val grunnIkkeReduksjonType: String? = null,
    @JsonProperty("persongrunnlagId")
    val persongrunnlagId: Long? = null,
    @JsonIgnore
    val changeStamp: String? = null,
    @JsonProperty("version")
    val version: Int? = null
)

enum class InntektsgrunnlagType(val code: String) {
    ARBEIDSINNTEKT("FORINTARB"),
    NAERINGSINNTEKT("FORINTNAE"),
    INNTEKT_UTLAND("FORINTUTL"),
    PENSJON_UTLAND("FORPENUTL"),
    ANDRE_YTELSER("FORINTAND")
}