package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.person

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.person.parallellesannheter.ParallelleSannheterService
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.person.pdl.*
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class PersonService(
    private val pdlClient: PdlClient,
    private val parallelleSannheterService: ParallelleSannheterService,
) {

    fun getFodselsdato(pid: String): LocalDate {
        val fodselsdato = pdlClient.performQuery(PdlQueryBuilder.getFoedselQuery(pid)).foedsel
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
}
