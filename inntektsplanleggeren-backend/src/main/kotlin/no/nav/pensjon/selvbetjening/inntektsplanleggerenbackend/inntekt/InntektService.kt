package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.enhetsregister.EregService
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.dto.*
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.*
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.Inntektsgrunnlag
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.InntektsgrunnlagType
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.Pensjonsdata
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.Month
import java.time.temporal.TemporalAdjusters
import java.time.temporal.TemporalAdjusters.firstDayOfMonth
import java.time.temporal.TemporalAdjusters.lastDayOfMonth

@Service
class InntektService(
    private val inntektskomponentClient: InntektskomponentClient,
    private val eregService: EregService
) {
    fun getInntekterHittilIAar(
        pid: String,
        pensjonsdata: Pensjonsdata,
        simuleringsaar: Int
    ): InntekterHittilIAar {
        if (LocalDate.now().year != simuleringsaar) {
            return InntekterHittilIAar(emptyList(), emptyList(), emptyList(), emptyList())
        }
        val arbeidsinntekterOgPensjonsgivendeYtelser =
            fetchArbeidsinntektOgPensjonsgivendeYtelser(pid, pensjonsdata)
        val pensjonFraAndreEnnFolketrygden =
            fetchPensjonFraAndreEnnFolketrygden(pid, pensjonsdata)

        return InntekterHittilIAar(
            arbeidsinntektOgPensjonsgivendeYtelser = arbeidsinntekterOgPensjonsgivendeYtelser[pid] ?: emptyList(),
            pensjonerFraAndreEnnFolketrygden = pensjonFraAndreEnnFolketrygden[pid],
            arbeidsinntektOgPensjonsgivendeYtelserEps = arbeidsinntekterOgPensjonsgivendeYtelser[pensjonsdata.epsPid],
            pensjonerFraAndreEnnFolketrygdenEps = pensjonFraAndreEnnFolketrygden[pensjonsdata.epsPid]
        )
    }

    fun getForventedeInntekter(
        pid: String,
        pensjonsdata: Pensjonsdata,
        simuleringsaar: Int
    ): ForventedeInntekterSummary {
        val allForventedeInntekterRelatedToPid =
            inntektskomponentClient.hentForventetInntekt(
                pid,
                listOf(simuleringsaar)
            ).forventetInntektListe
                ?.filter { it.hendelse != Inntektshendelse.VARSLET.code }

        val forventedeInntekter = if (pensjonsdata.hasOpenKravWithInntekter()) {
            getForventedeInntekterFromAapentKrav(pensjonsdata)
        } else {
            getForventedeInntekterFromInntektskomponent(
                allForventedeInntekterRelatedToPid,
                pensjonsdata
            )
        }

        return ForventedeInntekterSummary(
            forventedeInntekter,
            allForventedeInntekterRelatedToPid?.let {
                calculateSumBenyttedeInntekterBruker(it, pensjonsdata.hasBarnetillegg())
            } ?: 0,
            if (pensjonsdata.hasEpsWithFellesbarn()) {
                allForventedeInntekterRelatedToPid?.let { calculateSumBenyttedeInntekterEps(it) } ?: 0
            } else null
        )
    }

    private fun getForventedeInntekterFromAapentKrav(
        pensjonsdata: Pensjonsdata
    ): ForventedeInntekter {
        val inntektsgrunnlagFromKravBruker = pensjonsdata.inntekterFromOpenKravBruker
        val inntektsgrunnlagFromKravEps = pensjonsdata.inntekterFromOpenKravEps
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
                pensjonUtland = if (pensjonsdata.hasBarnetillegg()) {
                    getMostRecentInntektsgrunnlagOfTypeAsPersoninntekt(
                        inntektsgrunnlagFromKravBruker,
                        InntektsgrunnlagType.PENSJON_UTLAND
                    )
                } else null,
                andrePensjonsgivendeYtelser = if (pensjonsdata.hasBarnetillegg()) {
                    getMostRecentInntektsgrunnlagOfTypeAsPersoninntekt(
                        inntektsgrunnlagFromKravBruker,
                        InntektsgrunnlagType.ANDRE_YTELSER
                    )
                } else null
            ),
            eps = if (pensjonsdata.hasEpsWithFellesbarn()) {
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
        pensjonsdata: Pensjonsdata
    ): ForventedeInntekter {
        return ForventedeInntekter(
            bruker = PersonInntekter(
                arbeidsinntekt = allForventedeInntekterRelatedToPid?.let {
                    getMostRecentInntektOfTypeAsPersoninntekt(
                        it,
                        Inntektstype.ARBEIDSINNTEKT_BRUKER.code
                    )
                },
                naeringsinntekt = allForventedeInntekterRelatedToPid?.let {
                    getMostRecentInntektOfTypeAsPersoninntekt(
                        it,
                        Inntektstype.NAERINGSINNTEKT_BRUKER.code
                    )
                },
                inntektUtland = allForventedeInntekterRelatedToPid?.let {
                    getMostRecentInntektOfTypeAsPersoninntekt(
                        it,
                        Inntektstype.UTENLANDSINNTEKT_BRUKER.code
                    )
                },
                andrePensjonsgivendeYtelser = if (pensjonsdata.hasBarnetillegg()) {
                    allForventedeInntekterRelatedToPid?.let {
                        getMostRecentInntektOfTypeAsPersoninntekt(
                            it,
                            Inntektstype.ANDRE_YTELSER_BRUKER.code
                        )
                    }
                } else null,
                pensjonUtland = if (pensjonsdata.hasBarnetillegg()) {
                    allForventedeInntekterRelatedToPid?.let {
                        getMostRecentInntektOfTypeAsPersoninntekt(
                            it,
                            Inntektstype.PENSJON_UTLAND_BRUKER.code
                        )
                    }
                } else null,
            ),
            eps = if (pensjonsdata.hasEpsWithFellesbarn()) {
                PersonInntekter(
                    arbeidsinntekt = allForventedeInntekterRelatedToPid?.let {
                        getMostRecentInntektOfTypeAsPersoninntekt(
                            it,
                            Inntektstype.ARBEIDSINNTEKT_EPS.code
                        )
                    },
                    naeringsinntekt = allForventedeInntekterRelatedToPid?.let {
                        getMostRecentInntektOfTypeAsPersoninntekt(
                            it,
                            Inntektstype.NAERINGSINNTEKT_EPS.code
                        )
                    },
                    inntektUtland = allForventedeInntekterRelatedToPid?.let {
                        getMostRecentInntektOfTypeAsPersoninntekt(
                            it,
                            Inntektstype.UTENLANDSINNTEKT_EPS.code
                        )
                    },
                    andrePensjonsgivendeYtelser = allForventedeInntekterRelatedToPid?.let {
                        getMostRecentInntektOfTypeAsPersoninntekt(
                            it,
                            Inntektstype.ANDRE_YTELSER_EPS.code
                        )
                    },
                    pensjonUtland = allForventedeInntekterRelatedToPid?.let {
                        getMostRecentInntektOfTypeAsPersoninntekt(
                            it,
                            Inntektstype.PENSJON_UTLAND_EPS.code
                        )
                    }
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
        allInntekter: List<ForventetInntekt>,
        inntektstype: String
    ): Personinntekt? {
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
        pensjonsdata: Pensjonsdata,
    ): Map<String, List<Maanedsinntekt>> =
        fetchInntekter(pid, pensjonsdata, "UfoereA-Inntekt")

    private fun fetchPensjonFraAndreEnnFolketrygden(
        pid: String,
        pensjonsdata: Pensjonsdata,
    ): Map<String, List<Maanedsinntekt>> =
        if (pensjonsdata.hasBarnetillegg()) {
            fetchInntekter(
                pid,
                pensjonsdata,
                "UfoereBarnetilleggA-inntekt"
            )
        } else emptyMap()

    private fun fetchInntekter(
        pid: String,
        pensjonsdata: Pensjonsdata,
        inntektFilterCode: String
    ): Map<String, List<Maanedsinntekt>> {
        val inntektOgYtelsePerIdentList = inntektskomponentClient
            .hentAbonnerteInntekterBolk(
                constructAbonnerteInntekterIdentOgPerioder(pid, pensjonsdata),
                inntektFilterCode,
                decideFormal(pensjonsdata.hasBarnetillegg())
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
                    abonnertInntekt.maaned.monthValue
                )
            }
        }
        return emptyList()
    }

    private fun convertSumOpplysningspliktigToMaanedsinntekt(
        sumOpplysningspliktig: SumOpplysningspliktig,
        maaned: Int
    ): Maanedsinntekt? {
        if (sumOpplysningspliktig.avviksbeskrivelse.isNullOrEmpty()) {
            val aktorNameMap = mutableMapOf<String, String>()
            return Maanedsinntekt(
                maaned,
                sumOpplysningspliktig.beloep ?: 0.0,
                getAktorName(aktorNameMap, sumOpplysningspliktig.opplysningspliktig)
            )
        }
        return null
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
        pensjonsdata: Pensjonsdata
    ): List<AbonnerteInntekterIdentOgPeriode> =
        if (pensjonsdata.hasEpsWithFellesbarn()) {
            listOf(
                createAbonnerteInntekterIdentOgPeriode(pid, pensjonsdata.uforeFomDato),
                createAbonnerteInntekterIdentOgPeriode(pensjonsdata.epsPid!!, pensjonsdata.uforeFomDato)
            )
        } else {
            listOf(createAbonnerteInntekterIdentOgPeriode(pid, pensjonsdata.uforeFomDato))
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
