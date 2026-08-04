package no.nav.ufore.inntektsplanleggeren.inntekt.dto

import java.time.YearMonth

data class HentAbonnerteInntekterBolkResponse(
    val abonnerteInntekterPerIdentListe: List<AbonnerteInntekterPerIdent>?,
    val unntakForIdentListe: List<UnntakForIdent>?
)

data class AbonnerteInntekterPerIdent(
    val ident: Aktoer,
    var abonnerteInntekterMaanedListe: List<AbonnerteInntekterMaaned>? = emptyList()
)

data class SumOpplysningspliktig(
    val beloep: Double? = null,
    val avviksbeskrivelse: String? = null,
    val opplysningspliktig: Aktoer
)

data class AbonnerteInntekterMaaned(
    val beloep: Double? = null,
    val avviksbeskrivelse: String? = null,
    val maaned: YearMonth,
    val sumOpplysningspliktigListe: List<SumOpplysningspliktig> = emptyList(),
)

data class UnntakForIdent(
    val ident: Aktoer,
    val unntaksmelding: String? = null
)

data class HentAbonnerteInntekterBolkRequest(
    val ainntektsfilter: String,
    val filterversjon: String?,
    val formaal: String,
    val abonnerteInntekterIdentOgPeriodeListe: List<AbonnerteInntekterIdentOgPeriode>
)
