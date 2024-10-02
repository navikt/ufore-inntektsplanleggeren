package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto

data class GrunnlagForInntekter(
    val uforeHeleAaret: Boolean,
    val barnetilleggSaerkullsbarn: Boolean,
    val barnetilleggFellesbarn: Boolean,
    val epsPid: String?,
    val inntekterFromOpenKrav: List<Inntektsgrunnlag>
)
