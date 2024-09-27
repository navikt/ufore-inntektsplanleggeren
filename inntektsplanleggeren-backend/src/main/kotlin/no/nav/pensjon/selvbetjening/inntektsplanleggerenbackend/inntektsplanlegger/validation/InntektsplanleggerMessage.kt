package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation

data class InntektsplanleggerMessage(
    val messageCode: InntektsplanleggerMessageCode,
    val details: String = messageCode.details,
    val type: InntektsplanleggerMessageType = messageCode.type,
    val metadata: Map<MetadataKey, String> = mapOf(),
)


enum class InntektsplanleggerMessageCode(val type: InntektsplanleggerMessageType, val details: String) {
    USER_HAS_NO_UFORE(InntektsplanleggerMessageType.ERROR, "For å bruke inntektsplanleggeren må bruker ha uføretrygd"),
    USER_HAS_NO_LOPENDE_VEDTAK_YET(
        InntektsplanleggerMessageType.ERROR,
        "Bruker kan ikke registrere inntektsendring før vedkommendes vedtak har blitt løpende"
    ),
    ILLEGAL_INNTEKT_FIELD_VALUE(InntektsplanleggerMessageType.ERROR, "Inntekter som oppgis kan ikke være mindre enn 0"),
    ARBEIDSINNTEKT_GIVEN_SMALLER_THAN_HITTIL_I_AAR(
        InntektsplanleggerMessageType.ERROR,
        "Den oppgitte arbeidsinntekten er mindre enn det som er tjent hittil i år. Oppgitt arbeidsinntekt må minst være lik som det personen har tjent hittil i år."
    ),
    ANDRE_YTELSER_SMALLER_THAN_HITTIL_I_AAR(
        InntektsplanleggerMessageType.ERROR,
        "Den oppgitte verdien for andre pensjonsgivende ytelser er mindre enn det personen har fått av ytelser hittil i år. Oppgitt beløp må minst være lik som det personen har fått hittil i år."
    ),
    ONE_OR_MORE_INNTEKT_HAS_STATUS_REGISTRERT(InntektsplanleggerMessageType.WARNING, "Bruker har en tidligere registrert inntektsendring som fremdeles er under behandling."),
    EPS_INNTEKT_CHANGED(InntektsplanleggerMessageType.WARNING,"Bruker har endret en av EPS sine inntekter sammenlignet med det som tidligere var registrert som EPS sin inntekt.")
}

enum class InntektsplanleggerMessageType {
    ERROR,
    WARNING,
    INFO
}

enum class FieldReference {
    ARBEIDSINNTEKT_BRUKER,
    NAERINGSINNTEKT_BRUKER,
    INNTEKT_UTLAND_BRUKER,
    PENSJON_UTLAND_BRUKER,
    ANDRE_YTELSER_BRUKER,
    ARBEIDSINNTEKT_EPS,
    NAERINGSINNTEKT_EPS,
    INNTEKT_UTLAND_EPS,
    PENSJON_UTLAND_EPS,
    ANDRE_YTELSER_EPS
}

enum class MetadataKey {
    AFFECTED_FIELD
}