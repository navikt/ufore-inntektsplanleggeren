package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger

import no.nav.pensjon.selvbetjening.alderspensjonendringssoknadbackend.metrics.*
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.InntektService
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.Maanedsinntekt
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
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.Uforetrygd
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.StatusInnsendingResponse
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.person.PersonService
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security.TokenService
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.util.NowProvider
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneId

@Service
class InntektsplanleggerService(
    private val penClient: PenClient,
    private val validator: Validator,
    private val inntektService: InntektService,
    private val simuleringService: SimuleringService,
    private val tokenService: TokenService,
    private val nowProvider: NowProvider,
    private val personService: PersonService
) {

    fun sendInntektsendring(
        pid: String,
        simuleringsaar: Int,
        oppgitteInntekter: ForventedeInntekter
    ): InntektsplanleggerenSendResponse {
        val innsendingsTidspunkt = LocalDateTime.now(ZoneId.of("Europe/Paris"))
        val simulering = simulerInntektsendring(pid, simuleringsaar, oppgitteInntekter)
        var response: InntektsplanleggerenSendResponse

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
            response = InntektsplanleggerenSendResponse(simulering.messages, status, innsendingsTidspunkt)
        } else {
            response = InntektsplanleggerenSendResponse(
                simulering.messages,
                InnsendingStatus.IKKE_SENDT_VALIDERING_FEILET,
                innsendingsTidspunkt
            )


        }

        SendInntektsplanleggerMetricsCounter.count(response)
        return response
    }

    fun simulerInntektsendring(
        pid: String,
        simuleringsAar: Int,
        oppgitteInntekter: ForventedeInntekter
    ): SimuleringResponse {
        val uforetrygd = penClient.fetchInntektsplanleggerData(pid, getSimuleringFomDato(simuleringsAar))
        val gjeldendeForventedeInntekter =
            uforetrygd?.let { inntektService.getForventedeInntekter(pid, it, simuleringsAar) }
        val validationResult = validator.validateUserAndInputBeforeSimulering(
            uforetrygd,
            oppgitteInntekter,
            gjeldendeForventedeInntekter,
            pid,
            simuleringsAar,
            getAktuelleAar(uforetrygd?.hasLopendeUforeVedtakThisYear, uforetrygd?.hasLopendeUforeVedtakNextYear)
        )

        val response: SimuleringResponse

        if (validationResult.none { it.type == InntektsplanleggerMessageType.ERROR }) {
            val simuleringData = simuleringService.simulerInntektsendring(
                pid = pid,
                forventedeInntekterOppgitt = oppgitteInntekter,
                forventedeInntekter = gjeldendeForventedeInntekter!!,
                simuleringsaar = simuleringsAar,
                simuleringFomDato = getSimuleringFomDato(simuleringsAar)
            )
            response = SimuleringResponse(
                validationResult + simuleringData.valideringsresultat,
                simuleringData.simuleringsresultat
            )

        } else {
            response = SimuleringResponse(validationResult, null)
        }

        SimulateInntektsplanleggerMetricsCounter.count(response)

        return response
    }

    fun hentAarligeInntekter(pid: String, aar: Int): InntekterResponse? {
        return hentInntekter(pid, aar, LocalDate.of(aar, Month.JANUARY, 1), true)
    }

    fun hentInntekter(pid: String, simuleringsaar: Int, fetchForventedeInntekter: Boolean): InntekterResponse? {
        return hentInntekter(pid, simuleringsaar, getSimuleringFomDato(simuleringsaar), fetchForventedeInntekter)
    }

    private fun hentInntekter(pid: String, simuleringsaar: Int, simuleringsdato: LocalDate, fetchForventedeInntekter: Boolean
    ): InntekterResponse? {
        val uforetrygd =
            penClient.fetchInntektsplanleggerData(pid, simuleringsdato) ?: return null
        val inntekterHittilIAar = inntektService.getInntekterHittilIAar(
            pid,
            uforetrygd,
            simuleringsaar
        )

        val response = InntekterResponse(
            arbeidsinntektOgYtelserHittilIAar = accumulateAllInntekterForSameMonth(inntekterHittilIAar.arbeidsinntektOgPensjonsgivendeYtelser),
            pensjonFraAndreHittilIAar = accumulateAllInntekterForSameMonth(inntekterHittilIAar.pensjonerFraAndreEnnFolketrygden),
            forventedeInntekter = if (fetchForventedeInntekter) {
                inntektService.getForventedeInntekter(
                    pid,
                    uforetrygd,
                    simuleringsaar
                ).mostRecentForventedeInntekterRegistrertAndBenyttet.toDto()
            } else null,
            uforeHeleAaret = uforetrygd.uforeHeleAaret,
            epsPid = uforetrygd.epsPid?.let { uforetrygd.epsPid.substring(0, 6) + "*****" }
        )

        InntekterInntektsplanleggerMetricsCounter.count()

        return response
    }

    fun hentInitielleData(
        pid: String,
        simuleringsaar: Int
    ): InntektsplanleggerenInitialResponse {
        val uforetrygd = penClient.fetchInntektsplanleggerData(pid, getSimuleringFomDato(simuleringsaar))
        val aktuelleAar = uforetrygd.let {
            getAktuelleAar(
                it?.hasLopendeUforeVedtakThisYear,
                it?.hasLopendeUforeVedtakNextYear
            )
        }
        val messages = validator.validateUserInitialData(uforetrygd, aktuelleAar)
        val navn = personService.getNavn(pid)
        val loggetInnSom = tokenService.determineLoggedInUser()

        val response = InntektsplanleggerenInitialResponse(
            messages,
            mapInntektsplanleggerenInitialData(pid, uforetrygd, aktuelleAar, messages),
            pid,
            navn,
            loggetInnSom
        )

        InitiateInntektsplanleggerMetricsCounter.count(response)

        return response
    }

    fun hentStatus(
        fnr: String,
        simuleringsAar: Int,
        innsendingsTidspunkt: LocalDateTime
    ): InntektsplanleggerenStatusResponse? {

        val penResponse: StatusInnsendingResponse? =
            penClient.fetchInntektsplanleggerStatus(fnr, getSimuleringFomDato(simuleringsAar), innsendingsTidspunkt)
        val forventetInntekt = hentInntekter(fnr, simuleringsAar, true)
        var response: InntektsplanleggerenStatusResponse? = null

        if (penResponse != null) {
            response = InntektsplanleggerenStatusResponse(
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

        StatusInntektsplanleggerMetricsCounter.count(response)

        return response
    }

    private fun mapInntektsplanleggerenInitialData(
        pid: String,
        uforetrygd: Uforetrygd?,
        aktuelleAar: List<Int>,
        messages: List<InntektsplanleggerMessage>
    ): InntektsplanleggerenInitialData? {
        if (uforetrygd != null && messages.none { it.type == InntektsplanleggerMessageType.ERROR }) {
            val forventedeInntekter = getAktuelleAarForInntekt(aktuelleAar).associateWith { inntektService.getForventedeInntekter(pid, uforetrygd, it) }

            return InntektsplanleggerenInitialData(
                forventetInntekt = forventedeInntekter.map { it.key to it.value.sumBenyttedeInntekterBruker }.toMap(),
                forventetInntektAnnenForelder = forventedeInntekter.map { it.key to it.value.sumBenyttedeInntekterEps }.toMap(),
                inntektsgrense = uforetrygd.inntektsgrense,
                kompensasjonsgrad = uforetrygd.kompensasjonsgrad,
                grenseStoppAvUfoeretrygd = uforetrygd.grenseStoppAvUfoeretrygd,
                hasGjenlevendeTillegg = uforetrygd.hasGjenlevendeTillegg,
                hasBarneTilleggFellesbarn = uforetrygd.barnetilleggFellesbarn,
                hasBarnetilleggSaerkullsbarn = uforetrygd.barnetilleggSaerkullsbarn,
                hasVarigTilrettelagtArbeid = uforetrygd.hasVarigTilrettelagtArbeid,
                aktuelleAar = aktuelleAar,
                annetRelevantAar = getAnnetRelevantAar()
            )
        }
        return null
    }

    private fun getAnnetRelevantAar(): Int? =
        if (isMonthDecember()) {
            nowProvider.now().year
        } else null

    private fun getAktuelleAarForInntekt(aktuelleAar: List<Int>): List<Int> =
        if (isMonthDecember()) {
            (listOf(nowProvider.now().year) + aktuelleAar).distinct()
        } else {
            aktuelleAar
        }

    private fun getAktuelleAar(
        hasLopendeUforeVedtakThisYear: Boolean?,
        hasLopendeUforeVedtakNextYear: Boolean?
    ): List<Int> {
        val today = nowProvider.now()
        val isMonthBeforeOctober = today.month.value < Month.OCTOBER.value

        if (hasLopendeUforeVedtakThisYear == null || hasLopendeUforeVedtakNextYear == null) {
            return emptyList()
        }
        if (isMonthBeforeOctober && hasLopendeUforeVedtakThisYear) {
            return listOf(today.year)
        }
        if (isMonthDecember() && (hasLopendeUforeVedtakNextYear || hasLopendeUforeVedtakThisYear)) {
            return listOf(today.year + 1)
        }
        if (!isMonthBeforeOctober && hasLopendeUforeVedtakThisYear) {
            return listOf(today.year, today.year + 1)
        }
        if (!isMonthBeforeOctober && hasLopendeUforeVedtakNextYear) {
            return listOf(today.year + 1)
        }
        return emptyList()
    }

    private fun isMonthDecember() = nowProvider.now().month.value == Month.DECEMBER.value

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