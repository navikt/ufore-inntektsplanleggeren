package no.nav.ufore.inntektsplanleggeren.enhetsregister.dto


data class Organisasjon(
    val organisasjonsnummer: String,
    val navn: Organisasjonsnavn?,
    val enhetstype: String?,
    val adresse: Adresse?
)
data class Adresse(
    val type: String,
    val adresselinje1: String?,
    val postnummer: String?,
    val landkode: String?,
    val kommunenummer: String?,
    val bruksperiode: Periode?,
    val gyldighetsperiode: Periode?
)

data class Organisasjonsnavn(
    val sammensattnavn: String?,
    val navnelinje1: String?,
    val bruksperiode: Periode?,
    val gyldighetsperiode: Periode?
)

data class Periode(
    val fom: String? = null
)
