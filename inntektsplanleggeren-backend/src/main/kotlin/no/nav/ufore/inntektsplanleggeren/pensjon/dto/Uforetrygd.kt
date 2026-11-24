package no.nav.ufore.inntektsplanleggeren.pensjon.dto

import java.time.LocalDate


data class Uforetrygd(
    val bunnfradrag: Int,
    val bunnfradragNesteAar: Int,
    val reduksjonsprosent: Double,
    val reduksjonsprosentNesteAar: Double,
    val inntektstak: Int,
    val inntektstakNesteAar: Int,
    val hasLopendeUforeVedtakThisYear: Boolean,
    val hasLopendeUforeVedtakNextYear: Boolean,
    val uforeHeleAaret: Boolean,
    val barnetilleggSaerkullsbarn: Boolean,
    val barnetilleggFellesbarn: Boolean,
    val epsPid: String?,
    val inntekterFromOpenKravBruker: List<Inntektsgrunnlag>?,
    val inntekterFromOpenKravEps: List<Inntektsgrunnlag>?,
    val uforeFomDato: LocalDate?
) {

    fun hasBarnetillegg() = barnetilleggFellesbarn || barnetilleggSaerkullsbarn

    fun hasEpsWithFellesbarn() = epsPid != null && barnetilleggFellesbarn

    fun hasOpenKravWithInntekter() =
        inntekterFromOpenKravBruker != null && ((epsPid != null && inntekterFromOpenKravEps != null) || (epsPid == null && inntekterFromOpenKravEps == null))
}
