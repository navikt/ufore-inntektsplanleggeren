package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation

data class InntektsplanleggerMessage(val messageCode: InntektsplanleggerMessageCode, val details: String = messageCode.details, val type: InntektsplanleggerMessageType = messageCode.type)


enum class InntektsplanleggerMessageCode(val type: InntektsplanleggerMessageType, val details: String) {
    USER_HAS_NO_UFORE(InntektsplanleggerMessageType.ERROR, "For å bruke inntektsplanleggeren må bruker ha uføretrygd"),
    USER_HAS_NO_LOPENDE_VEDTAK_YET(InntektsplanleggerMessageType.ERROR, "Bruker kan ikke registrere inntektsendring før vedkommendes vedtak har blitt løpende")
}

enum class InntektsplanleggerMessageType {
    ERROR,
    WARNING,
    INFO
}