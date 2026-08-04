package no.nav.ufore.inntektsplanleggeren.person.pdl

import com.fasterxml.jackson.annotation.JsonProperty

data class HentPersonResponse(@JsonProperty("data") val data: PdlData?,
                              @JsonProperty("errors") val errors: List<PdlError>?){

}

data class PdlData(@JsonProperty("hentPerson") val hentPdlPerson: PdlPerson?)
