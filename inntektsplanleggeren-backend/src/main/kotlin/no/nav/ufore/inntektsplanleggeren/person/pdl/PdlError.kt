package no.nav.ufore.inntektsplanleggeren.person.pdl

import com.fasterxml.jackson.annotation.JsonProperty

data class PdlError(@JsonProperty("message") val message: String,
                    @JsonProperty("locations") val locations: List<PdlErrorLocation>,
                    @JsonProperty("path") val path: List<String>?,
                    @JsonProperty("extensions") val extensions: PdlErrorExtension?)

data class PdlErrorLocation(@JsonProperty("line") val line: Int,
                            @JsonProperty("column") val column: Int)

data class PdlErrorExtension(@JsonProperty("code") val code: PdlErrorCodes?,
                             @JsonProperty("classification") val classification: String?)

enum class PdlErrorCodes {
    @JsonProperty("unauthenticated") UNAUTHENTICATED,
    @JsonProperty("unauthorized") UNAUTHORIZED,
    @JsonProperty("not_found") NOT_FOUND,
    @JsonProperty("bad_request") BAD_REQUEST,
    @JsonProperty("server_error") SERVER_ERROR
}