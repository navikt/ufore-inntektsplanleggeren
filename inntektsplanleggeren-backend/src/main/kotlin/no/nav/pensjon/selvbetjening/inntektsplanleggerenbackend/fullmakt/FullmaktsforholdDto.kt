package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.fullmakt

import com.fasterxml.jackson.annotation.JsonProperty

data class FullmaktsforholdDto (
    @JsonProperty("hasValidRepresentasjonsforhold") val hasValidRepresentasjonsforhold: Boolean,
    @JsonProperty("fullmaktsgiverNavn") val fullmaktsgiverNavn: String?
)
