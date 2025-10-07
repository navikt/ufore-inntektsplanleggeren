package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.inntekt


data class InntekterResponse(
    val arbeidsinntektOgYtelserHittilIAar: List<AccumulatedMaanedsinntekt>,
    val pensjonFraAndreHittilIAar: List<AccumulatedMaanedsinntekt>?,
    val forventedeInntekter: ForventedeInntekter?,
    val uforeHeleAaret: Boolean,
    val epsPid: String?,
    val pid: String
)

data class AccumulatedMaanedsinntekt(val maned: Int, val belop: Double?, val inntektsgivere: List<String>)