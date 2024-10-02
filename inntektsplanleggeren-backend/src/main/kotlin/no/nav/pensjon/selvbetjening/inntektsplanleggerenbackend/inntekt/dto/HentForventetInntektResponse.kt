package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.dto

import java.time.LocalDateTime

data class HentForventetInntektResponse (
    val norskident: String?,
    val forventetInntektListe: List<ForventetInntekt>? = emptyList(),
)

data class ForventetInntekt (
    val aar: String,
    val beloep: Int,
    val type: String,
    val hendelse: String? = null,
    val informasjonsopphav: String? = null,
    val endringstidspunkt: LocalDateTime? = null
)