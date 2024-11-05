package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model

data class InntekterHittilIAar(
    val arbeidsinntektOgPensjonsgivendeYtelser: List<Maanedsinntekt>,
    val pensjonerFraAndreEnnFolketrygden: List<Maanedsinntekt>?,
    val arbeidsinntektOgPensjonsgivendeYtelserEps: List<Maanedsinntekt>?,
    val pensjonerFraAndreEnnFolketrygdenEps: List<Maanedsinntekt>?
)

data class Maanedsinntekt(val maned: Int, val belop: Double, val utbetaltFra: String)
