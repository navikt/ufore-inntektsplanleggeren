package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.person

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.person.parallellesannheter.ParallelleSannheterService
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.person.pdl.*
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security.TokenService
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.planlegger.PersonNotFoundException
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class PersonService(
    val pdlClient: PdlClient,
    val parallelleSannheterService: ParallelleSannheterService,
    val tokenService: TokenService
) {

    fun getFodselsdato(pid: String): LocalDate {
        val fodselsdato = pdlClient.performQuery(PdlQueryBuilder.getFoedselQuery(pid)).foedsel
        return parallelleSannheterService.decideFodselsdato(fodselsdato)
            ?: throw IllegalStateException("Not able to determine fodselsdato for user")
    }

    fun getPersondataForFullmaktsgiver(pidFullmaktsgiver: String): Persondata {
        val person = pdlClient.performQuery(PdlQueryBuilder.getPersondataQuery(pidFullmaktsgiver))
        return mapPdlPersonToPersondata(pidFullmaktsgiver, person)
    }

    fun getPersondataForMottaker(pidMottaker: String): Persondata? {
        val person: PdlPerson
        try {
            val adressebeskyttelse = getAdressebeskyttelsesgrad(pidMottaker)
            if (tokenService.isUserLoggedInAsSaksbehandler()) {
                person = if (isUgradert(adressebeskyttelse)
                    || isStrengtFortroligAndSaksbehandlerHasAccess(adressebeskyttelse)
                    || isFortroligAndSaksbehandlerHasAccess(adressebeskyttelse)
                ) {
                    //User logged in as saksbehandler, mottaker is either ugradert or saksbehandler has necessary access
                    pdlClient.performQuery(PdlQueryBuilder.getPersondataQuery(pidMottaker))
                } else {
                    //User logged in as saksbehandler, mottaker is adressebeskyttet and saksbehandler lacks access
                    return Persondata(pidMottaker, getFodselsdatoWithElevatedPriveleges(pidMottaker), Navn(null, null, null))
                }
            } else if (isUgradert(adressebeskyttelse)) {
                //User logged in as borger, and mottaker not adressebeskyttet
                person = pdlClient.performQueryWithElevatedPriveleges(PdlQueryBuilder.getPersondataQuery(pidMottaker))
            } else {
                //User logged in as borger, and mottaker is adressebeskyttet
                return Persondata(pidMottaker, getFodselsdatoWithElevatedPriveleges(pidMottaker), Navn(null, null, null))
            }
        } catch (exception: PersonNotFoundException) {
            return null
        }
        return mapPdlPersonToPersondata(pidMottaker, person)
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

    private fun mapPdlPersonToPersondata(pid: String, pdlPerson: PdlPerson): Persondata{
        val fodselsdato = parallelleSannheterService.decideFodselsdato(pdlPerson.foedsel)
        val navn = parallelleSannheterService.decideNavn(pdlPerson.navn)
        if (navn == null || fodselsdato == null) {
            throw IllegalStateException("Could not determine navn or fodselsdato for person")
        }

        return Persondata(pid, fodselsdato, Navn(navn.fornavn, navn.mellomnavn, navn.etternavn))
    }

    private fun getFodselsdatoWithElevatedPriveleges(pid: String): LocalDate {
        val fodselsdato = pdlClient.performQueryWithElevatedPriveleges(PdlQueryBuilder.getFoedselQuery(pid)).foedsel
        return parallelleSannheterService.decideFodselsdato(fodselsdato)
            ?: throw IllegalStateException("Not able to determine fodselsdato for user")
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

data class Persondata(val pid: String, val fodselsdato: LocalDate, val navn: Navn){
    fun fulltNavn(): String? {
        if (navn.fornavn == null || navn.etternavn == null) {
            return null
        } else if (navn.mellomnavn == null) {
            return "${navn.fornavn} ${navn.etternavn}"
        }
        return "${navn.fornavn} ${navn.mellomnavn} ${navn.etternavn}"
    }
}

data class Navn(val fornavn: String?, val mellomnavn: String?, val etternavn: String?)
