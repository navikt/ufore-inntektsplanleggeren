package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate
import java.time.LocalDateTime

data class InntektsplanleggerenStatusResponse(
    val status: StatusKrav,
    val sakId: Long?,
    val maandedligeUtbetalinger: MaandedligeUtbetalinger1? = null,
    @JsonProperty("registeringsTidspunktEndring")
    @JsonFormat(pattern = "yyyy-MM-dd' 'HH:mm:ss")
    val registeringsTidspunktEndring: LocalDateTime?,
    val mottarBarnetilleggForFellesBarn: Boolean = false,
    val forventetAarligInntekt: Int? = null,
    val forventetAarligInntektEps: Int? = null,)

enum class StatusKrav{
    BEHANDLET_MEDFOERER_ENDRING,
    BEHANDLET_MEDFOERER_INGEN_ENDRING,
    TIL_BEHANDLING
}

data class MaandedligeUtbetalinger1(
    @JsonProperty("fom")
    @JsonFormat(pattern = "yyyy-MM-dd")
    val fom: LocalDate,
    val beloep: Int
)