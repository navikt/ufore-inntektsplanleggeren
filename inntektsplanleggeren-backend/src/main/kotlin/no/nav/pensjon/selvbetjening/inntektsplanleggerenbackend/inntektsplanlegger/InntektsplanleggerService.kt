package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.Maanedsinntekt
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.InntektService
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.inntekt.AccumulatedMaanedsinntekt
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.inntekt.ForventedeInntekter
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.inntekt.InntekterResponse
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.simulering.SimuleringResponse
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.simulering.SimuleringService
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation.InntektsplanleggerMessage
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation.InntektsplanleggerMessageType
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.validation.Validator
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.PenClient
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.BehandlingStatus
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.Pensjonsdata
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.StatusInnsendingResponse
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security.TokenService
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.util.NowProvider
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month

@Service
class InntektsplanleggerService(
    private val penClient: PenClient,
    private val validator: Validator,
    private val inntektService: InntektService,
    private val simuleringService: SimuleringService,
    private val tokenService: TokenService,
    private val nowProvider: NowProvider
) {

    fun sendInntektsendring(
        pid: String,
        simuleringsaar: Int,
        oppgitteInntekter: ForventedeInntekter
    ): InntektsplanleggerenSendResponse {
        val innsendingsTidspunkt = LocalDateTime.now()
        val simulering = simulerInntektsendring(pid, simuleringsaar, oppgitteInntekter)
        if (simulering.messages.none { it.type == InntektsplanleggerMessageType.ERROR }) {
            val initiertAv = tokenService.determineLoggedInUser()
            val innsending = penClient.sendInntektsendring(
                pid = pid,
                virk = getSimuleringFomDato(simuleringsaar),
                simulertTotalbelopNetto = simulering.result?.sum?.monthly?.after!!,
                inntektsgrunnlagListe = oppgitteInntekter.bruker.mapToInntektsgrunnlag(simuleringsaar, initiertAv),
                inntektsgrunnlagListeEps = oppgitteInntekter.eps?.mapToInntektsgrunnlag(simuleringsaar, initiertAv)
            )
            val status =
                when (innsending.status) {
                    BehandlingStatus.AUTOMATISK_BEHANDLING.name -> {
                        InnsendingStatus.AUTOMATISK_BEHANDLING
                    }

                    BehandlingStatus.INNTEKT_LAGRET.name -> {
                        InnsendingStatus.INNTEKT_LAGRET_INGEN_BEHANDLING
                    }

                    else -> InnsendingStatus.IKKE_SENDT
                }
            return InntektsplanleggerenSendResponse(simulering.messages, status, innsendingsTidspunkt)
        }
        return InntektsplanleggerenSendResponse(simulering.messages, InnsendingStatus.IKKE_SENDT_VALIDERING_FEILET, innsendingsTidspunkt)
    }

    fun simulerInntektsendring(
        pid: String,
        simuleringsAar: Int,
        oppgitteInntekter: ForventedeInntekter
    ): SimuleringResponse {
        val pensjonsdata = penClient.fetchInntektsplanleggerData(pid, getSimuleringFomDato(simuleringsAar))
        val gjeldendeForventedeInntekter =
            pensjonsdata?.let { inntektService.getForventedeInntekter(pid, it, simuleringsAar) }
        val validationResult = validator.validateUserAndInputBeforeSimulering(
            pensjonsdata,
            oppgitteInntekter,
            gjeldendeForventedeInntekter,
            pid,
            simuleringsAar
        )

        if (validationResult.none { it.type == InntektsplanleggerMessageType.ERROR }) {
            val simuleringData = simuleringService.simulerInntektsendring(
                pid = pid,
                forventedeInntekterOppgitt = oppgitteInntekter,
                forventedeInntekter = gjeldendeForventedeInntekter!!,
                simuleringsaar = simuleringsAar,
                simuleringFomDato = getSimuleringFomDato(simuleringsAar)
            )

            return SimuleringResponse(
                validationResult + simuleringData.valideringsresultat,
                simuleringData.simuleringsresultat
            )
        }

        return SimuleringResponse(validationResult, null)
    }

    fun constructInntekterResponse(pid: String, simuleringsaar: Int): InntekterResponse? {
        val pensjonsdata =
            penClient.fetchInntektsplanleggerData(pid, getSimuleringFomDato(simuleringsaar)) ?: return null
        val inntekterHittilIAar = inntektService.getInntekterHittilIAar(
            pid,
            pensjonsdata,
            simuleringsaar
        )

        return InntekterResponse(
            arbeidsinntektOgYtelserHittilIAar = accumulateAllInntekterForSameMonth(inntekterHittilIAar.arbeidsinntektOgPensjonsgivendeYtelser),
            pensjonFraAndreHittilIAar = accumulateAllInntekterForSameMonth(inntekterHittilIAar.pensjonerFraAndreEnnFolketrygden),
            forventedeInntekter = inntektService.getForventedeInntekter(
                pid,
                pensjonsdata,
                simuleringsaar
            ).mostRecentForventedeInntekterRegistrertAndBenyttet.toDto(),
            uforeHeleAaret = pensjonsdata.uforeHeleAaret
        )
    }

    fun constructInitialInntektsplanleggerResponse(
        pid: String,
        simuleringsaar: Int
    ): InntektsplanleggerenInitialResponse {
        val pensjonsdata = penClient.fetchInntektsplanleggerData(pid, getSimuleringFomDato(simuleringsaar))
        val aktuelleAar = pensjonsdata?.let {
            getAktuelleAar(
                it.hasLopendeUforeVedtakThisYear,
                it.hasLopendeUforeVedtakNextYear
            )
        }
        val messages = validator.validateUserInitialData(pensjonsdata, aktuelleAar)
        return InntektsplanleggerenInitialResponse(
            messages,
            mapInntektsplanleggerenInitialData(pid, pensjonsdata, simuleringsaar, messages)
        )
    }

    fun constructStatusResponse(
        fnr: String,
        simuleringsAar: Int,
        innsendingsTidspunkt: LocalDateTime
    ): InntektsplanleggerenStatusResponse? {

        val penResponse: StatusInnsendingResponse? =
            penClient.fetchInntektsplanleggerStatus(fnr, getSimuleringFomDato(simuleringsAar), innsendingsTidspunkt)
        val forventetInntekt = constructInntekterResponse(fnr, simuleringsAar)
        if (penResponse != null) {
            return InntektsplanleggerenStatusResponse(
                penResponse.status,
                penResponse.sakId,
                penResponse.maandedligeUtbetalinger?.fom?.let {
                    MaandedligeUtbetalinger1(
                        it,
                        penResponse.maandedligeUtbetalinger.beloep
                    )
                },
                penResponse.endringRegistertTidspunkt,
                penResponse.mottarBarnetilleggForFellesBarn,
                forventetInntekt?.forventedeInntekter?.bruker?.sum(),
                forventetInntekt?.forventedeInntekter?.eps?.sum()
            )
        }
        return null
    }

    private fun mapInntektsplanleggerenInitialData(
        pid: String,
        pensjonsdata: Pensjonsdata?,
        simuleringsaar: Int,
        messages: List<InntektsplanleggerMessage>
    ): InntektsplanleggerenInitialData? {
        if (pensjonsdata != null && messages.none { it.type == InntektsplanleggerMessageType.ERROR }) {
            val forventedeInntekter = inntektService.getForventedeInntekter(pid, pensjonsdata, simuleringsaar)
            return InntektsplanleggerenInitialData(
                forventetInntekt = forventedeInntekter.sumBenyttedeInntekterBruker,
                forventetInntektAnnenForelder = forventedeInntekter.sumBenyttedeInntekterEps,
                inntektsgrense = pensjonsdata.inntektsgrense,
                kompensasjonsgrad = pensjonsdata.kompensasjonsgrad,
                grenseStoppAvUfoeretrygd = pensjonsdata.grenseStoppAvUfoeretrygd,
                hasGjenlevendeTillegg = pensjonsdata.hasGjenlevendeTillegg,
                hasBarneTilleggFellesbarn = pensjonsdata.barnetilleggFellesbarn,
                grenseStoppAvBarnetilleggFellesbarn = pensjonsdata.grenseStoppAvBarnetilleggFellesbarn,
                fribelopBarnetilleggFellesbarn = pensjonsdata.fribelopBarnetilleggFellesbarn,
                hasBarnetilleggSaerkullsbarn = pensjonsdata.barnetilleggSaerkullsbarn,
                grenseStoppAvBarnetilleggSaerkullsbarn = pensjonsdata.grenseStoppAvBarnetilleggSaerkullsbarn,
                fribelopBarnetilleggSaerkullsbarn = pensjonsdata.fribelopBarnetilleggSaerkullsbarn,
                hasVarigTilrettelagtArbeid = pensjonsdata.hasVarigTilrettelagtArbeid,
                aktuelleAar = getAktuelleAar(
                    pensjonsdata.hasLopendeUforeVedtakThisYear,
                    pensjonsdata.hasLopendeUforeVedtakNextYear
                )
            )
        }
        return null
    }

    private fun getAktuelleAar(
        hasLopendeUforeVedtakThisYear: Boolean,
        hasLopendeUforeVedtakNextYear: Boolean
    ): List<Int> {
        val today = nowProvider.now()
        val isMonthBeforeOctober = today.month.value < Month.OCTOBER.value

        if (isMonthBeforeOctober && hasLopendeUforeVedtakThisYear) {
            return listOf(today.year)
        }
        if (!isMonthBeforeOctober && hasLopendeUforeVedtakThisYear) {
            return listOf(today.year, today.year + 1)
        }
        if (!isMonthBeforeOctober && hasLopendeUforeVedtakNextYear) {
            return listOf(today.year + 1)
        }
        return emptyList()
    }

    private fun accumulateAllInntekterForSameMonth(maanedsinntekter: List<Maanedsinntekt>?): List<AccumulatedMaanedsinntekt> {
        val inntekterEachMonth = mutableMapOf<Int, MutableList<Maanedsinntekt>>()

        maanedsinntekter?.forEach { maanedsinntekt ->
            run {
                if (inntekterEachMonth.containsKey(maanedsinntekt.maned)) {
                    inntekterEachMonth[maanedsinntekt.maned]?.add(maanedsinntekt)
                } else {
                    inntekterEachMonth[maanedsinntekt.maned] = mutableListOf(maanedsinntekt)
                }
            }
        }

        return inntekterEachMonth.keys.map { maaned ->
            AccumulatedMaanedsinntekt(
                maaned,
                inntekterEachMonth[maaned]?.sumOf { it.belop } ?: 0.0,
                inntekterEachMonth[maaned]?.map { it.utbetaltFra } ?: emptyList()
            )
        }
    }

    private fun getSimuleringFomDato(simuleringsaar: Int): LocalDate =
        if (simuleringsaar > nowProvider.now().year)
            LocalDate.of(simuleringsaar, Month.JANUARY, 1)
        else
            LocalDate.now().plusMonths(1).withDayOfMonth(1)
}