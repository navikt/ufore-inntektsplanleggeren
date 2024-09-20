package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt

data class InntekterHittilIAar(
    val arbeidsinntektOgPensjonsgivendeYtelser: List<Maanedsinntekt>,
    val pensjonerFraAndreEnnFolketrygden: List<Maanedsinntekt>,
    val arbeidsinntektOgPensjonsgivendeYtelserEps: List<Maanedsinntekt>?,
    val pensjonerFraAndreEnnFolketrygdenEps: List<Maanedsinntekt>?
)
