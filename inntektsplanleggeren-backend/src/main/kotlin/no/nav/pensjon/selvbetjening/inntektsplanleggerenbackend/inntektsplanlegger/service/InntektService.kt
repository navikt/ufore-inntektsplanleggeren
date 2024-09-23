package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.service

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.enhetsregister.EregService
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.*
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

    private fun fetchArbeidsinntektOgPensjonsgivendeYtelser(
        pid: String,
        epsPid: String?,
        barnetilleggFellesbarn: Boolean,
        barnetilleggSaerkullsbarn: Boolean,
    ): Map<String, List<Maanedsinntekt>> =
        fetchInntekter(pid, epsPid, barnetilleggFellesbarn, barnetilleggSaerkullsbarn, "UFOERE_A_INNTEKT")

    private fun fetchPensjonFraAndreEnnFolketrygden(
        pid: String,
        epsPid: String?,
        barnetilleggFellesbarn: Boolean,
        barnetilleggSaerkullsbarn: Boolean,
    ): Map<String, List<Maanedsinntekt>> =
        if (barnetilleggFellesbarn || barnetilleggSaerkullsbarn) {
            fetchInntekter(pid, epsPid, barnetilleggFellesbarn, barnetilleggSaerkullsbarn, "UFOERE_BT_A_INNTEKT")
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

        inntektOgYtelsePerIdentList.forEach { person ->
            inntektYtelseMap[person.ident.identifikator] =
                person.abonnerteInntekterMaanedListe.flatMap { convertAbonnertInntektToMaanedsinntekter(it) }
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
            "UFOERETRYGDBARNETILLEGG"
        } else {
            "UFOERE"
        }


}
