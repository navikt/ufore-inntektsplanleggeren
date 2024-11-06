package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.person

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.person.parallellesannheter.ParallelleSannheterService
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.person.pdl.*
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security.TokenService
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class PersonService(
    private val pdlClient: PdlClient,
    private val parallelleSannheterService: ParallelleSannheterService,
    private val tokenService: TokenService
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

    fun hasSaksbehandlerAccessToPid(pid: String): Boolean {
        val adressebeskyttelse = getAdressebeskyttelsesgrad(pid)
        return (isUgradert(adressebeskyttelse)
                || isStrengtFortroligAndSaksbehandlerHasAccess(adressebeskyttelse)
                || isFortroligAndSaksbehandlerHasAccess(adressebeskyttelse))
    }

    fun hasAdressebeskyttelse(pid: String): Boolean {
        val adressebeskyttelsesgrad = getAdressebeskyttelsesgrad(pid)
        if (adressebeskyttelsesgrad == null || adressebeskyttelsesgrad == PdlAdressebeskyttelsesgradering.UGRADERT) {
            return false
        }
        return true
    }

    private fun isUgradert(adressebeskyttelse: PdlAdressebeskyttelsesgradering?) =
        adressebeskyttelse == null || adressebeskyttelse == PdlAdressebeskyttelsesgradering.UGRADERT

    private fun isFortroligAndSaksbehandlerHasAccess(adressebeskyttelse: PdlAdressebeskyttelsesgradering?) =
        adressebeskyttelse == PdlAdressebeskyttelsesgradering.FORTROLIG && tokenService.isUserInFortroligGroup()

    private fun isStrengtFortroligAndSaksbehandlerHasAccess(adressebeskyttelse: PdlAdressebeskyttelsesgradering?) =
        (adressebeskyttelse == PdlAdressebeskyttelsesgradering.STRENGT_FORTROLIG || adressebeskyttelse == PdlAdressebeskyttelsesgradering.STRENGT_FORTROLIG_UTLAND)
                && tokenService.isUserInStrengtFortroligGroup()

    private fun getAdressebeskyttelsesgrad(pid: String): PdlAdressebeskyttelsesgradering? {
        val adressebeskyttelse =
            pdlClient.performQueryWithElevatedPriveleges(PdlQueryBuilder.getAdressebeskyttelseQuery(pid)).adressebeskyttelse
        return parallelleSannheterService.decideAdressebeskyttelse(adressebeskyttelse)?.gradering
    }
}
