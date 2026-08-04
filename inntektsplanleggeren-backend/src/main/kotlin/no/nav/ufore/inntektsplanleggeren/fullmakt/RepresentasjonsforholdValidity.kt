package no.nav.ufore.inntektsplanleggeren.fullmakt

import com.fasterxml.jackson.annotation.JsonProperty

data class RepresentasjonsforholdValidity (
    @JsonProperty("hasValidRepresentasjonsforhold") val hasValidRepresentasjonsforhold: Boolean,
    @JsonProperty("representertNavn") val representertNavn: String?,
    @JsonProperty("representertPidKryptert") val representertPidKryptert: String,
    @JsonProperty("representertPid") val representertPid: String,
)
