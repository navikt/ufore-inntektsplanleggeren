package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation

data class InntektsplanleggerMessage(
    val messageCode: InntektsplanleggerMessageCode,
    val details: String = messageCode.details,
    val type: InntektsplanleggerMessageType = messageCode.type,
    val metadata: Map<MetadataKey, Any?> = mapOf(),
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
    ONE_OR_MORE_INNTEKT_HAS_STATUS_REGISTRERT(
        InntektsplanleggerMessageType.WARNING,
        "Bruker har en tidligere registrert inntektsendring som fremdeles er under behandling."
    ),
    EPS_INNTEKT_CHANGED(
        InntektsplanleggerMessageType.WARNING,
        "Bruker har endret en av EPS sine inntekter sammenlignet med det som tidligere var benyttet som EPS sin inntekt."
    ),
    ILLEGAL_MONTH_DECEMBER_THIS_YEAR(
        InntektsplanleggerMessageType.ERROR,
        "Man kan ikke simulere inntektsendring for inneværende år når man er i desember, da inntektsendring ikke er mulig for dette året lenger.",
    ),
    INNTEKT_ONLY_RELEVANT_WHEN_BARNETILLEGG(
        InntektsplanleggerMessageType.ERROR,
        "Inntekt fra andre ytelser og pensjon fra utlandet er kun relevant når bruker har barnetillegg for fellesbarn eller særkullsbarn."
    ),
    MISSING_RELEVANT_INNTEKTER_WHEN_BARNETILLEGG(
        InntektsplanleggerMessageType.ERROR,
        "Inntekt fra andre ytelser og pensjon fra utlandet må oppgis når bruker har barnetillegg for fellesbarn eller særkullsbarn. Hvis bruker ikke har noen inntekter for disse kategoriene, skal 0 sendes inn."
    ),
    EPS_INNTEKT_ONLY_RELEVANT_WHEN_BARNETILLEGG_FELLESBARN(
        InntektsplanleggerMessageType.ERROR,
        "EPS sine inntekter er kun relevante å oppgi når bruker har barnetillegg for fellesbarn."
    ),
    MISSING_RELEVANT_EPS_INNTEKT_WHEN_BARNETILLEGG_FELLESBARN(
        InntektsplanleggerMessageType.ERROR,
        "Når bruker har barnetillegg for fellesbarn skal alltid alle EPS sine inntekter oppgis. Hvis EPS ikke har en eller flere av disse inntektene, skal 0 sendes inn."
    ),
    FIELD_CAN_NOT_BE_NULL(
        InntektsplanleggerMessageType.ERROR,
        "Feltet må alltid ha en verdi. Hvis bruker ikke har noen inntekt for feltet, skal verdien være 0."
    ),
    OPPGITT_INNTEKT_OVER_INNTEKTSTAK(
        InntektsplanleggerMessageType.WARNING,
        "Bruker har oppgitt inntekt som overstiger 80% av oppjustert IFU (inntekt før uføretrygd), dette medfører at uføretrygd blir redusert til 0"
    ),
    OPPGITT_INNTEKT_GIVES_LOWER_UFORETRYGD_THAN_ALREADY_UTBETALT(
        InntektsplanleggerMessageType.WARNING,
        "Bruker har oppgitt inntekt som er høyere enn tidligere oppgitt. Uføretrygd som allerede er utbetalt dette året er høyere enn det bruker ville fått dette året med den oppgitte inntekten. Bruker må tilbakebetale i etteroppgjør."
    ),
    OPPGITT_INNTEKT_GIVES_MORE_UFORETRYGD_THAN_ALREADY_UTBETALT(
        InntektsplanleggerMessageType.WARNING,
        "Bruker har oppgitt inntekt som er lavere enn tidligere oppgitt. Brukers uføretrygd vil derfor ikke reduseres mot inntekt resten av året. Hvis for lite utbetales i uføretrygd, vil bruker få etterbetalt i etteroppgjør."
    ),
    FAKTOROMREGNET_ELLER_MANUELT_OVERSTYRT(
        InntektsplanleggerMessageType.WARNING,
        "Brukers nåværende sak er faktoromregnet eller manuelt overstyrt. Det er dermed ikke mulig å simulere på saken, men bruker kan likevel sende inn inntektsendring."
    ),
    OPEN_INNTEKTSENDRING_KRAV(
        InntektsplanleggerMessageType.WARNING,
        "Bruker har et åpent krav om inntektsendring. Dette medfører at siste rapporterte inntekt ikke nødvendigvis er den som vises som dagens inntekt for brukeren, da siste innsendte fremdeles er under behandling."
    ),
    SIMULERING_CONTAINS_MOTREGNING(
        InntektsplanleggerMessageType.WARNING,
        "Simuleringen inneholder en eller flere ytelseskomponenter som har en motregning. Dette kan bety at simuleringsresultatet blir misvisende å vise til bruker."
    ),
    FORVENTET_INNTEKT_THIS_YEAR_USED_NEXT_YEAR_INFO(
        InntektsplanleggerMessageType.INFO,
        "Hvis bruker ikke sender inn ny forventet inntekt for neste år, brukes brukers forventede inntekt som er registrert for i år. Inntekten oppjusteres ved årsskiftet hvis det ikke meldes inn inntekt for neste år."
    ),
    CAN_NOT_REPORT_INNTEKT_FOR_THIS_YEAR(
        InntektsplanleggerMessageType.INFO,
        "I desember kan bruker se hvilke inntekter som er registrert for dette året. Bruker kan også legge inn nye inntekter for neste år, men kan ikke legge til endring i inntekt for inneværende år, fordi endring i utbetalingen ikke vil skje før til neste år." +
                "Heading"
    )
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
    AFFECTED_FIELD,
    INNTEKTSTAK,
    SUM_OVER_INNTEKTSTAK,
    UFORE_HITTIL_I_AR,
    SUM_SIMULERT_UFORETRYGD,
    SUM_HITTIL_I_AAR
}