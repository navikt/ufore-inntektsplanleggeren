package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.person.pdl

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.person.parallellesannheter.dto.ParallellSannhet
import java.time.LocalDate
import java.time.LocalDateTime

data class PdlPerson(
    @JsonProperty("foedselsdato") val foedselsdato: List<PdlFoedselsdato>?,
    @JsonProperty("navn") val navn: List<PdlNavn>?,
    @JsonProperty("adressebeskyttelse") val adressebeskyttelse: List<PdlAdressebskyttelse>?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PdlFoedselsdato(
    @JsonProperty("foedselsdato") val foedselsdato: LocalDate?,
    @JsonProperty("metadata") val metadata: PdlMetadata?,
    @JsonProperty("folkeregistermetadata") override val folkeregistermetadata: PdlFolkeregisterMetadata?
) : ParallellSannhet(metadata, folkeregistermetadata)

data class PdlNavn(
    @JsonProperty("fornavn") val fornavn: String,
    @JsonProperty("mellomnavn") val mellomnavn: String?,
    @JsonProperty("etternavn") val etternavn: String,
    @JsonProperty("metadata") val metadata: PdlMetadata?,
    @JsonProperty("folkeregistermetadata") override val folkeregistermetadata: PdlFolkeregisterMetadata?
): ParallellSannhet(metadata, folkeregistermetadata)

data class PdlAdressebskyttelse(
    @JsonProperty("gradering") val gradering: PdlAdressebeskyttelsesgradering,
    @JsonProperty("metadata") val metadata: PdlMetadata?,
    @JsonProperty("folkeregistermetadata") override val folkeregistermetadata: PdlFolkeregisterMetadata?
): ParallellSannhet(metadata, folkeregistermetadata)

enum class  PdlAdressebeskyttelsesgradering{
    //Also known as "kode 7"
    FORTROLIG,

    //Also known as "kode 6"
    STRENGT_FORTROLIG,

    //Adressebeskyttelse specified by forvaltningsloven paragraph 19
    STRENGT_FORTROLIG_UTLAND,

    //No adressebeskyttelse
    UGRADERT;
}


data class PdlMetadata(
    @JsonProperty("master") val master: String?,
    @JsonProperty("endringer") val endringer: List<PdlMetadataEndring>?,
    @JsonProperty("historisk") val historisk: Boolean?
)

data class PdlMetadataEndring(
    @JsonFormat(shape = JsonFormat.Shape.STRING) @JsonProperty("registrert") val registrert: LocalDateTime?,
    @JsonProperty("kilde") val kilde: String?,
    @JsonProperty("registrertAv") val registrertAv: String?,
    @JsonProperty("systemkilde") val systemkilde: String?,
    @JsonProperty("type") val type: String?
)

data class PdlFolkeregisterMetadata(
    @JsonFormat(shape = JsonFormat.Shape.STRING) @JsonProperty("ajourholdstidspunkt") val ajourholdstidspunkt: LocalDateTime?,
    @JsonFormat(shape = JsonFormat.Shape.STRING) @JsonProperty("gyldighetstidspunkt") val gyldighetstidspunkt: LocalDateTime?
)

