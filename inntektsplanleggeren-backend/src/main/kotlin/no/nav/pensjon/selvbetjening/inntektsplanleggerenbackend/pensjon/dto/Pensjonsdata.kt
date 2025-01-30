package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto

import java.time.LocalDate

data class Pensjonsdata(
    val inntektsgrense: Int,
    val kompensasjonsgrad: Double,
    val grenseStoppAvUfoeretrygd: Int,
    val hasLopendeUforeVedtakThisYear: Boolean,
    val hasLopendeUforeVedtakNextYear: Boolean,
    val hasVarigTilrettelagtArbeid: Boolean,
    val hasGjenlevendeTillegg: Boolean,
    val uforeHeleAaret: Boolean,
    val barnetilleggSaerkullsbarn: Boolean,
    val grenseStoppAvBarnetilleggSaerkullsbarn: Int?,
    val fribelopBarnetilleggSaerkullsbarn: Int?,
    val barnetilleggFellesbarn: Boolean,
    val grenseStoppAvBarnetilleggFellesbarn: Int?,
    val fribelopBarnetilleggFellesbarn: Int?,
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
