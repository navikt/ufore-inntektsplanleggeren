package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.StatusKrav
import java.time.LocalDate
import java.time.LocalDateTime

data class StatusInnsendingResponse(
    val status: StatusKrav,
    val sakId: Long?,
    val maandedligeUtbetalinger: MaandedligeUtbetalinger? = null,
    val endringRegistertTidspunkt: LocalDateTime?,
    val mottarBarnetilleggForFellesBarn: Boolean = false
)

data class MaandedligeUtbetalinger(
    val fom: LocalDate,
    val beloep: Int
)