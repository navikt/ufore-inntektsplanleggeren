package no.nav.ufore.inntektsplanleggeren.person

import no.nav.ufore.inntektsplanleggeren.person.parallellesannheter.ParallelleSannheterService
import no.nav.ufore.inntektsplanleggeren.person.pdl.PdlAdressebeskyttelsesgradering
import no.nav.ufore.inntektsplanleggeren.person.pdl.PdlClient
import no.nav.ufore.inntektsplanleggeren.person.pdl.PdlQueryBuilder
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class PersonService(
    private val pdlClient: PdlClient,
    private val parallelleSannheterService: ParallelleSannheterService,
) {

    fun getFodselsdato(pid: String): LocalDate {
        val fodselsdato = pdlClient.performQuery(PdlQueryBuilder.getFoedselQuery(pid)).foedselsdato
        return parallelleSannheterService.decideFodselsdato(fodselsdato)
            ?: throw IllegalStateException("Not able to determine fodselsdato for user")
    }

    fun getAgeAtYear(fodselsdato: LocalDate, year: Int): Int {
        if (year < fodselsdato.year) {
            throw IllegalStateException("Illegal year value for opptjening")
        }
        return year - fodselsdato.year
    }

    fun hasAdressebeskyttelse(pid: String): Boolean {
        val adressebeskyttelsesgrad = getAdressebeskyttelsesgrad(pid)
        if (adressebeskyttelsesgrad == null || adressebeskyttelsesgrad == PdlAdressebeskyttelsesgradering.UGRADERT) {
            return false
        }
        return true
    }

    fun getAdressebeskyttelsesgrad(pid: String): PdlAdressebeskyttelsesgradering? {
        val adressebeskyttelse =
            pdlClient.performQueryWithElevatedPriveleges(PdlQueryBuilder.getAdressebeskyttelseQuery(pid)).adressebeskyttelse
        return parallelleSannheterService.decideAdressebeskyttelse(adressebeskyttelse)?.gradering
    }

    fun getNavn(pid: String): String? {
        pdlClient.performQuery(PdlQueryBuilder.getPersonQuery(pid)).let {
            val navn = parallelleSannheterService.decideNavn(it.navn)
            return navn?.let {
                "${it.fornavn} ${it.mellomnavn?.let { mellomnavn -> "$mellomnavn " } ?: ""}${it.etternavn}"
            }
        }
    }
}
