package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.enhetsregister.EregService
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.dto.*
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.*
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.Inntektsgrunnlag
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.InntektsgrunnlagType
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.Uforetrygd
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.util.NowProvider
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters
import java.time.temporal.TemporalAdjusters.lastDayOfMonth

@Service
class InntektService(
    private val inntektskomponentClient: InntektskomponentClient,
    private val eregService: EregService,
    private val nowProvider: NowProvider
) {
    fun getInntekterHittilIAar(
        pid: String,
        uforetrygd: Uforetrygd,
        simuleringsaar: Int
    ): InntekterHittilIAar {
        if (LocalDate.now().year != simuleringsaar) {
            return InntekterHittilIAar(emptyList(), emptyList(), emptyList(), emptyList())
        }
        val arbeidsinntekterOgPensjonsgivendeYtelser =
            fetchArbeidsinntektOgPensjonsgivendeYtelser(pid, uforetrygd)
        val pensjonFraAndreEnnFolketrygden =
            fetchPensjonFraAndreEnnFolketrygden(pid, uforetrygd)

        return InntekterHittilIAar(
            arbeidsinntektOgPensjonsgivendeYtelser = arbeidsinntekterOgPensjonsgivendeYtelser[pid] ?: emptyList(),
            pensjonerFraAndreEnnFolketrygden = pensjonFraAndreEnnFolketrygden[pid],
            arbeidsinntektOgPensjonsgivendeYtelserEps = arbeidsinntekterOgPensjonsgivendeYtelser[uforetrygd.epsPid],
            pensjonerFraAndreEnnFolketrygdenEps = pensjonFraAndreEnnFolketrygden[uforetrygd.epsPid]
        )
    }

    fun getForventedeInntekter(
        pid: String,
        uforetrygd: Uforetrygd,
        simuleringsaar: Int
    ): ForventedeInntekterSummary {
        val allForventedeInntekterRelatedToPid =
            inntektskomponentClient.hentForventetInntekt(
                pid,
                listOf(simuleringsaar)
            ).forventetInntektListe
                ?.filter { it.hendelse != Inntektshendelse.VARSLET.code }

        val forventedeInntekter = if (uforetrygd.hasOpenKravWithInntekter()) {
            getForventedeInntekterFromAapentKrav(uforetrygd)
        } else {
            getForventedeInntekterFromInntektskomponent(
                allForventedeInntekterRelatedToPid,
                uforetrygd
            )
        }

        return ForventedeInntekterSummary(
            forventedeInntekter,
            allForventedeInntekterRelatedToPid?.let {
                calculateSumBenyttedeInntekterBruker(it, uforetrygd.hasBarnetillegg())
            } ?: 0,
            if (uforetrygd.hasEpsWithFellesbarn()) {
                allForventedeInntekterRelatedToPid?.let { calculateSumBenyttedeInntekterEps(it) } ?: 0
            } else null
        )
    }

    private fun getForventedeInntekterFromAapentKrav(
        uforetrygd: Uforetrygd
    ): ForventedeInntekter {
        val inntektsgrunnlagFromKravBruker = uforetrygd.inntekterFromOpenKravBruker
        val inntektsgrunnlagFromKravEps = uforetrygd.inntekterFromOpenKravEps
        return ForventedeInntekter(
            bruker = PersonInntekter(
                arbeidsinntekt = getMostRecentInntektsgrunnlagOfTypeAsPersoninntekt(
                    inntektsgrunnlagFromKravBruker!!,
                    InntektsgrunnlagType.ARBEIDSINNTEKT
                ),
                naeringsinntekt = getMostRecentInntektsgrunnlagOfTypeAsPersoninntekt(
                    inntektsgrunnlagFromKravBruker,
                    InntektsgrunnlagType.NAERINGSINNTEKT
                ),
                inntektUtland = getMostRecentInntektsgrunnlagOfTypeAsPersoninntekt(
                    inntektsgrunnlagFromKravBruker,
                    InntektsgrunnlagType.INNTEKT_UTLAND
                ),
                pensjonUtland = if (uforetrygd.hasBarnetillegg()) {
                    getMostRecentInntektsgrunnlagOfTypeAsPersoninntekt(
                        inntektsgrunnlagFromKravBruker,
                        InntektsgrunnlagType.PENSJON_UTLAND
                    )
                } else null,
                andrePensjonsgivendeYtelser = if (uforetrygd.hasBarnetillegg()) {
                    getMostRecentInntektsgrunnlagOfTypeAsPersoninntekt(
                        inntektsgrunnlagFromKravBruker,
                        InntektsgrunnlagType.ANDRE_YTELSER
                    )
                } else null
            ),
            eps = if (uforetrygd.hasEpsWithFellesbarn()) {
                PersonInntekter(
                    arbeidsinntekt = getMostRecentInntektsgrunnlagOfTypeAsPersoninntekt(
                        inntektsgrunnlagFromKravEps!!,
                        InntektsgrunnlagType.ARBEIDSINNTEKT
                    ),
                    naeringsinntekt = getMostRecentInntektsgrunnlagOfTypeAsPersoninntekt(
                        inntektsgrunnlagFromKravEps,
                        InntektsgrunnlagType.NAERINGSINNTEKT
                    ),
                    inntektUtland = getMostRecentInntektsgrunnlagOfTypeAsPersoninntekt(
                        inntektsgrunnlagFromKravEps,
                        InntektsgrunnlagType.INNTEKT_UTLAND
                    ),
                    pensjonUtland = getMostRecentInntektsgrunnlagOfTypeAsPersoninntekt(
                        inntektsgrunnlagFromKravEps,
                        InntektsgrunnlagType.PENSJON_UTLAND
                    ),
                    andrePensjonsgivendeYtelser = getMostRecentInntektsgrunnlagOfTypeAsPersoninntekt(
                        inntektsgrunnlagFromKravEps,
                        InntektsgrunnlagType.ANDRE_YTELSER
                    )
                )
            } else null
        )
    }

    private fun getMostRecentInntektsgrunnlagOfTypeAsPersoninntekt(
        inntektsgrunnlagListe: List<Inntektsgrunnlag>,
        type: InntektsgrunnlagType
    ): Personinntekt {
        val belop = inntektsgrunnlagListe
            .sortedByDescending { it.fomDato }
            .sortedByDescending { it.endringstidspunkt }
            .firstOrNull { it.bruk && type.code == it.inntektType }?.belop
        return if (belop != null) {
            Personinntekt(belop, Inntektshendelse.REGISTRERT)
        } else {
            Personinntekt(0, Inntektshendelse.IKKE_REGISTRERT)
        }
    }

    private fun getForventedeInntekterFromInntektskomponent(
        allForventedeInntekterRelatedToPid: List<ForventetInntekt>?,
        uforetrygd: Uforetrygd
    ): ForventedeInntekter {
        return ForventedeInntekter(
            bruker = PersonInntekter(
                arbeidsinntekt = getMostRecentInntektOfTypeAsPersoninntekt(
                    allForventedeInntekterRelatedToPid,
                    Inntektstype.ARBEIDSINNTEKT_BRUKER.code
                ),
                naeringsinntekt = getMostRecentInntektOfTypeAsPersoninntekt(
                    allForventedeInntekterRelatedToPid,
                    Inntektstype.NAERINGSINNTEKT_BRUKER.code
                ),
                inntektUtland = getMostRecentInntektOfTypeAsPersoninntekt(
                    allForventedeInntekterRelatedToPid,
                    Inntektstype.UTENLANDSINNTEKT_BRUKER.code
                ),
                andrePensjonsgivendeYtelser = if (uforetrygd.hasBarnetillegg()) {
                    getMostRecentInntektOfTypeAsPersoninntekt(
                        allForventedeInntekterRelatedToPid,
                        Inntektstype.ANDRE_YTELSER_BRUKER.code
                    )
                } else null,
                pensjonUtland = if (uforetrygd.hasBarnetillegg()) {
                    getMostRecentInntektOfTypeAsPersoninntekt(
                        allForventedeInntekterRelatedToPid,
                        Inntektstype.PENSJON_UTLAND_BRUKER.code
                    )
                } else null,
            ),
            eps = if (uforetrygd.hasEpsWithFellesbarn()) {
                PersonInntekter(
                    arbeidsinntekt = getMostRecentInntektOfTypeAsPersoninntekt(
                        allForventedeInntekterRelatedToPid,
                        Inntektstype.ARBEIDSINNTEKT_EPS.code
                    ),
                    naeringsinntekt = getMostRecentInntektOfTypeAsPersoninntekt(
                        allForventedeInntekterRelatedToPid,
                        Inntektstype.NAERINGSINNTEKT_EPS.code
                    ),
                    inntektUtland = getMostRecentInntektOfTypeAsPersoninntekt(
                        allForventedeInntekterRelatedToPid,
                        Inntektstype.UTENLANDSINNTEKT_EPS.code
                    ),
                    andrePensjonsgivendeYtelser = getMostRecentInntektOfTypeAsPersoninntekt(
                        allForventedeInntekterRelatedToPid,
                        Inntektstype.ANDRE_YTELSER_EPS.code
                    ),
                    pensjonUtland = getMostRecentInntektOfTypeAsPersoninntekt(
                        allForventedeInntekterRelatedToPid,
                        Inntektstype.PENSJON_UTLAND_EPS.code
                    ),
                )
            } else null
        )
    }

    private fun calculateSumBenyttedeInntekterBruker(
        allForventedeInntekterRelatedToPid: List<ForventetInntekt>,
        hasBarnetillegg: Boolean
    ): Int {

        val onlyBenyttedeInntekter =
            allForventedeInntekterRelatedToPid.filter { it.hendelse == Inntektshendelse.BENYTTET.code }

        return listOfNotNull(
            Inntektstype.ARBEIDSINNTEKT_BRUKER,
            Inntektstype.NAERINGSINNTEKT_BRUKER,
            Inntektstype.UTENLANDSINNTEKT_BRUKER,
            if (hasBarnetillegg) {
                Inntektstype.ANDRE_YTELSER_BRUKER
            } else null,
            if (hasBarnetillegg) {
                Inntektstype.PENSJON_UTLAND_BRUKER
            } else null
        )
            .mapNotNull { getMostRecentInntektOfTypeAsPersoninntekt(onlyBenyttedeInntekter, it.code) }
            .sumOf { it.belop }
    }

    private fun calculateSumBenyttedeInntekterEps(allForventedeInntekterRelatedToPid: List<ForventetInntekt>): Int {
        val onlyBenyttedeInntekter =
            allForventedeInntekterRelatedToPid.filter { it.hendelse == Inntektshendelse.BENYTTET.code }
        return listOf(
            Inntektstype.ARBEIDSINNTEKT_EPS,
            Inntektstype.NAERINGSINNTEKT_EPS,
            Inntektstype.UTENLANDSINNTEKT_EPS,
            Inntektstype.ANDRE_YTELSER_EPS,
            Inntektstype.PENSJON_UTLAND_EPS
        )
            .mapNotNull { getMostRecentInntektOfTypeAsPersoninntekt(onlyBenyttedeInntekter, it.code) }
            .sumOf { it.belop }
    }

    private fun getMostRecentInntektOfTypeAsPersoninntekt(
        allInntekter: List<ForventetInntekt>?,
        inntektstype: String
    ): Personinntekt {
        if (allInntekter == null) {
            return Personinntekt(0, Inntektshendelse.IKKE_REGISTRERT)
        }

        val mostRecentInntektOfType = allInntekter
            .sortedByDescending { it.endringstidspunkt }
            .firstOrNull { it.type == inntektstype }
        if (mostRecentInntektOfType != null) {
            return Personinntekt(
                mostRecentInntektOfType.beloep,
                Inntektshendelse.getHendelseForCode(mostRecentInntektOfType.hendelse)
            )
        }
        return Personinntekt(0, Inntektshendelse.IKKE_REGISTRERT)
    }

    private fun fetchArbeidsinntektOgPensjonsgivendeYtelser(
        pid: String,
        uforetrygd: Uforetrygd,
    ): Map<String, List<Maanedsinntekt>> =
        fetchInntekter(pid, uforetrygd, "UfoereA-Inntekt")

    private fun fetchPensjonFraAndreEnnFolketrygden(
        pid: String,
        uforetrygd: Uforetrygd,
    ): Map<String, List<Maanedsinntekt>> =
        if (uforetrygd.hasBarnetillegg()) {
            fetchInntekter(
                pid,
                uforetrygd,
                "UfoereBarnetilleggA-inntekt"
            )
        } else emptyMap()

    private fun fetchInntekter(
        pid: String,
        uforetrygd: Uforetrygd,
        inntektFilterCode: String
    ): Map<String, List<Maanedsinntekt>> {
        val inntektOgYtelsePerIdentList = inntektskomponentClient
            .hentAbonnerteInntekterBolk(
                constructAbonnerteInntekterIdentOgPerioder(pid, uforetrygd),
                inntektFilterCode,
                decideFormal(uforetrygd.hasBarnetillegg()),
                pid
            )
            .abonnerteInntekterPerIdentListe
        val inntektYtelseMap = mutableMapOf<String, List<Maanedsinntekt>>()

        inntektOgYtelsePerIdentList?.forEach { person ->
            if (person.abonnerteInntekterMaanedListe == null) {
                person.abonnerteInntekterMaanedListe = emptyList() //TODO: Ikke den meste elegante løsningen
            }
            inntektYtelseMap[person.ident.identifikator] =
                person.abonnerteInntekterMaanedListe!!.flatMap { convertAbonnertInntektToMaanedsinntekter(it) }
        }

        return inntektYtelseMap
    }

    private fun convertAbonnertInntektToMaanedsinntekter(abonnertInntekt: AbonnerteInntekterMaaned): List<Maanedsinntekt> {
        if (abonnertInntekt.avviksbeskrivelse.isNullOrEmpty()) {
            return abonnertInntekt.sumOpplysningspliktigListe.mapNotNull {
                convertSumOpplysningspliktigToMaanedsinntekt(
                    it,
                    abonnertInntekt.maaned
                )
            }
        }
        return emptyList()
    }

    private fun convertSumOpplysningspliktigToMaanedsinntekt(
        sumOpplysningspliktig: SumOpplysningspliktig,
        maaned: YearMonth
    ): Maanedsinntekt? {
        if (sumOpplysningspliktig.avviksbeskrivelse.isNullOrEmpty() && isEtterRegistreringsfrist(maaned)) {
            val aktorNameMap = mutableMapOf<String, String>()
            return Maanedsinntekt(
                maaned.monthValue,
                sumOpplysningspliktig.beloep ?: 0.0,
                getAktorName(aktorNameMap, sumOpplysningspliktig.opplysningspliktig)
            )
        }
        return null
    }

    private fun isEtterRegistreringsfrist(maaned: YearMonth): Boolean {
        val now = nowProvider.now()
        val registreringsFrist = if (maaned.month == Month.DECEMBER) {
            LocalDate.of(maaned.year + 1, Month.JANUARY, 5)
        } else {
            LocalDate.of(maaned.year, maaned.month + 1, 5)
        }
        return now.isAfter(registreringsFrist)
    }

    private fun getAktorName(aktorNameMap: MutableMap<String, String>, aktor: Aktoer): String {
        if (aktor.aktoerType == "ORGANISASJON") {
            if (aktorNameMap.containsKey(aktor.identifikator)) {
                return aktorNameMap[aktor.identifikator]!!
            }
            val aktorName = eregService.getOrganisasjonsnavn(aktor.identifikator)
            aktorNameMap[aktor.identifikator] = aktorName
            return aktorName
        }
        return ""
    }

    private fun constructAbonnerteInntekterIdentOgPerioder(
        pid: String,
        uforetrygd: Uforetrygd
    ): List<AbonnerteInntekterIdentOgPeriode> =
        if (uforetrygd.hasEpsWithFellesbarn()) {
            listOf(
                createAbonnerteInntekterIdentOgPeriode(pid, uforetrygd.uforeFomDato),
                createAbonnerteInntekterIdentOgPeriode(uforetrygd.epsPid!!, uforetrygd.uforeFomDato)
            )
        } else {
            listOf(createAbonnerteInntekterIdentOgPeriode(pid, uforetrygd.uforeFomDato))
        }

    private fun createAbonnerteInntekterIdentOgPeriode(pid: String, uforeFom: LocalDate?): AbonnerteInntekterIdentOgPeriode {
        val year = LocalDate.now().year
        return AbonnerteInntekterIdentOgPeriode(
            ident = Aktoer(pid, "NATURLIG_IDENT"),
            spoerringPeriodeFom = decideSpoerringFom(uforeFom).toString(),
            spoerringPeriodeTom = LocalDate.of(year, Month.DECEMBER.value, 1).with(lastDayOfMonth()).toString()
        )
    }

    private fun decideSpoerringFom(uforeFom: LocalDate?): LocalDate {
        val firstDayThisYear = LocalDate.now().with(TemporalAdjusters.firstDayOfYear())
        if (uforeFom == null || uforeFom.isBefore(firstDayThisYear)) {
            return firstDayThisYear
        }
        return uforeFom
    }

    private fun decideFormal(isBarnetillegg: Boolean) =
        if (isBarnetillegg) {
            "Ufoeretrygdbarnetillegg"
        } else {
            "Ufoere"
        }
}
