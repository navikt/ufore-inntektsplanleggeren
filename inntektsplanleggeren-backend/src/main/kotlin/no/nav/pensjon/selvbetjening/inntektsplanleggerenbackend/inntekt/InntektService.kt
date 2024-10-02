package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.enhetsregister.EregService
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.dto.*
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.model.*
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.Inntektsgrunnlag
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.Pensjonsdata
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.Month
import java.time.temporal.TemporalAdjusters.lastDayOfMonth

@Service
class InntektService(
    private val inntektskomponentClient: InntektskomponentClient,
    private val eregService: EregService
) {
    fun getInntekterHittilIAar(
        pid: String,
        epsPid: String?,
        barnetilleggFellesbarn: Boolean,
        barnetilleggSaerkullsbarn: Boolean,
        simuleringsaar: Int
    ): InntekterHittilIAar {
        if (LocalDate.now().year != simuleringsaar) {
            return InntekterHittilIAar(emptyList(), emptyList(), emptyList(), emptyList())
        }
        val arbeidsinntekterOgPensjonsgivendeYtelser =
            fetchArbeidsinntektOgPensjonsgivendeYtelser(pid, epsPid, barnetilleggFellesbarn, barnetilleggSaerkullsbarn)
        val pensjonFraAndreEnnFolketrygden =
            fetchPensjonFraAndreEnnFolketrygden(pid, epsPid, barnetilleggFellesbarn, barnetilleggSaerkullsbarn)

        return InntekterHittilIAar(
            arbeidsinntektOgPensjonsgivendeYtelser = arbeidsinntekterOgPensjonsgivendeYtelser[pid] ?: emptyList(),
            pensjonerFraAndreEnnFolketrygden = pensjonFraAndreEnnFolketrygden[pid] ?: emptyList(),
            arbeidsinntektOgPensjonsgivendeYtelserEps = arbeidsinntekterOgPensjonsgivendeYtelser[epsPid],
            pensjonerFraAndreEnnFolketrygdenEps = pensjonFraAndreEnnFolketrygden[epsPid]
        )
    }

    fun getForventedeInntekter(
        pid: String,
        pensjonsdata: Pensjonsdata,
        simuleringsaar: Int
    ): ForventedeInntekterSummary {
        val hasBarnetillegg = pensjonsdata.barnetilleggFellesbarn || pensjonsdata.barnetilleggSaerkullsbarn
        val allForventedeInntekterRelatedToPid =
            inntektskomponentClient.hentForventetInntekt(
                pid,
                listOf(simuleringsaar)
            ).forventetInntektListe
                ?.filter { it.hendelse != Inntektshendelse.VARSLET.code }

        return ForventedeInntekterSummary(
            ForventedeInntekter(
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
                    andrePensjonsgivendeYtelser = if (hasBarnetillegg) {
                        allForventedeInntekterRelatedToPid?.let {
                            getMostRecentInntektOfTypeAsPersoninntekt(
                                it,
                                Inntektstype.ANDRE_YTELSER_BRUKER.code
                            )
                        }
                    } else null,
                    pensjonUtland = if (hasBarnetillegg) {
                        allForventedeInntekterRelatedToPid?.let {
                            getMostRecentInntektOfTypeAsPersoninntekt(
                                it,
                                Inntektstype.PENSJON_UTLAND_BRUKER.code
                            )
                        }
                    } else null,
                ),
                eps = if (pensjonsdata.epsPid != null && pensjonsdata.barnetilleggFellesbarn) {
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
            ), calculateSumBenyttedeInntekterBruker(
                allForventedeInntekterRelatedToPid,
                pensjonsdata.inntekterFromOpenKrav,
                hasBarnetillegg
            ),
            if (pensjonsdata.epsPid != null && pensjonsdata.barnetilleggFellesbarn) {
                calculateSumBenyttedeInntekterEps(allForventedeInntekterRelatedToPid)
            } else null
        )
    }

    private fun calculateSumBenyttedeInntekterBruker(
        allForventedeInntekterRelatedToPid: List<ForventetInntekt>,
        inntekterFromOpenKrav: List<Inntektsgrunnlag>?,
        hasBarnetillegg: Boolean
    ): Int {
        if (!inntekterFromOpenKrav.isNullOrEmpty()) {
            val sortedInntekter = inntekterFromOpenKrav.sortedByDescending { it.endringstidspunkt }
            val arbeidsinntekt = sortedInntekter.firstOrNull { it.bruk && it.inntektType == "FORINTARB" }
            val naeringsinntekt = sortedInntekter.firstOrNull { it.bruk && it.inntektType == "FORINTNAE" }
            val inntektUtland = sortedInntekter.firstOrNull { it.bruk && it.inntektType == "FORINTUTL" }

            return listOf(arbeidsinntekt, naeringsinntekt, inntektUtland).sumOf { it?.belop ?: 0 }
        }

        val onlyBenyttedeInntekter =
            allForventedeInntekterRelatedToPid.filter { it.type == Inntektshendelse.BENYTTET.code }

        val sumInntekter = listOfNotNull(
            getMostRecentInntektOfTypeAsPersoninntekt(
                onlyBenyttedeInntekter,
                Inntektstype.ARBEIDSINNTEKT_BRUKER.code
            ),
            getMostRecentInntektOfTypeAsPersoninntekt(
                onlyBenyttedeInntekter,
                Inntektstype.NAERINGSINNTEKT_BRUKER.code
            ),
            getMostRecentInntektOfTypeAsPersoninntekt(
                onlyBenyttedeInntekter,
                Inntektstype.UTENLANDSINNTEKT_BRUKER.code
            )
        ).sumOf { it.belop }

        val sumYtelserAndPensjon = if (hasBarnetillegg) {
            listOfNotNull(
                getMostRecentInntektOfTypeAsPersoninntekt(
                    onlyBenyttedeInntekter,
                    Inntektstype.ANDRE_YTELSER_BRUKER.code
                ),
                getMostRecentInntektOfTypeAsPersoninntekt(
                    onlyBenyttedeInntekter,
                    Inntektstype.PENSJON_UTLAND_BRUKER.code
                )
            ).sumOf { it.belop }
        } else 0

        return sumInntekter + sumYtelserAndPensjon
    }

    private fun calculateSumBenyttedeInntekterEps(allForventedeInntekterRelatedToPid: List<ForventetInntekt>): Int {
        val onlyBenyttedeInntekter =
            allForventedeInntekterRelatedToPid.filter { it.type == Inntektshendelse.BENYTTET.code }

        return listOfNotNull(
            getMostRecentInntektOfTypeAsPersoninntekt(
                onlyBenyttedeInntekter,
                Inntektstype.ARBEIDSINNTEKT_EPS.code
            ),
            getMostRecentInntektOfTypeAsPersoninntekt(
                onlyBenyttedeInntekter,
                Inntektstype.NAERINGSINNTEKT_EPS.code
            ),
            getMostRecentInntektOfTypeAsPersoninntekt(
                onlyBenyttedeInntekter,
                Inntektstype.UTENLANDSINNTEKT_EPS.code
            ),
            getMostRecentInntektOfTypeAsPersoninntekt(
                onlyBenyttedeInntekter,
                Inntektstype.ANDRE_YTELSER_EPS.code
            ),
            getMostRecentInntektOfTypeAsPersoninntekt(
                onlyBenyttedeInntekter,
                Inntektstype.PENSJON_UTLAND_EPS.code
            )
        ).sumOf { it.belop }
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
        return null
    }

    private fun fetchArbeidsinntektOgPensjonsgivendeYtelser(
        pid: String,
        epsPid: String?,
        barnetilleggFellesbarn: Boolean,
        barnetilleggSaerkullsbarn: Boolean,
    ): Map<String, List<Maanedsinntekt>> =
        fetchInntekter(pid, epsPid, barnetilleggFellesbarn, barnetilleggSaerkullsbarn, "UfoereA-Inntekt")

    private fun fetchPensjonFraAndreEnnFolketrygden(
        pid: String,
        epsPid: String?,
        barnetilleggFellesbarn: Boolean,
        barnetilleggSaerkullsbarn: Boolean,
    ): Map<String, List<Maanedsinntekt>> =
        if (barnetilleggFellesbarn || barnetilleggSaerkullsbarn) {
            fetchInntekter(
                pid,
                epsPid,
                barnetilleggFellesbarn,
                barnetilleggSaerkullsbarn,
                "UfoereBarnetilleggA-inntekt"
            )
        } else emptyMap()

    private fun fetchInntekter(
        pid: String,
        epsPid: String?,
        barnetilleggFellesbarn: Boolean,
        barnetilleggSaerkullsbarn: Boolean,
        inntektFilterCode: String
    ): Map<String, List<Maanedsinntekt>> {
        val inntektOgYtelsePerIdentList = inntektskomponentClient
            .hentAbonnerteInntekterBolk(
                constructAbonnerteInntekterIdentOgPerioder(pid, epsPid, barnetilleggFellesbarn),
                inntektFilterCode,
                decideFormal(barnetilleggFellesbarn || barnetilleggSaerkullsbarn)
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
        epsPid: String?,
        barnetilleggFellesbarn: Boolean
    ): List<AbonnerteInntekterIdentOgPeriode> =
        if (barnetilleggFellesbarn && epsPid != null) {
            listOf(createAbonnerteInntekterIdentOgPeriode(pid), createAbonnerteInntekterIdentOgPeriode(epsPid))
        } else {
            listOf(createAbonnerteInntekterIdentOgPeriode(pid))
        }

    private fun createAbonnerteInntekterIdentOgPeriode(pid: String): AbonnerteInntekterIdentOgPeriode {
        val year = LocalDate.now().year
        return AbonnerteInntekterIdentOgPeriode(
            ident = Aktoer(pid, "NATURLIG_IDENT"),
            spoerringPeriodeFom = LocalDate.of(year, Month.JANUARY.value, 1).toString(),
            spoerringPeriodeTom = LocalDate.of(year, Month.DECEMBER.value, 1).with(lastDayOfMonth()).toString()
        )
    }

    private fun decideFormal(isBarnetillegg: Boolean) =
        if (isBarnetillegg) {
            "Ufoeretrygdbarnetillegg"
        } else {
            "Ufoere"
        }


}
