package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.dto

data class InntektsplanleggerMessage(val messageCode: InntektsplanleggerMessageCode, val details: String = messageCode.details, val type: InntektsplanleggerMessageType = messageCode.type)


enum class InntektsplanleggerMessageCode(val type: InntektsplanleggerMessageType, val details: String) {
    USER_HAS_NO_UFORE(InntektsplanleggerMessageType.ERROR, "For å bruke inntektsplanleggeren må bruker ha uføretrygd ")
}

enum class InntektsplanleggerMessageType {
    ERROR,
    WARNING,
    INFO
}