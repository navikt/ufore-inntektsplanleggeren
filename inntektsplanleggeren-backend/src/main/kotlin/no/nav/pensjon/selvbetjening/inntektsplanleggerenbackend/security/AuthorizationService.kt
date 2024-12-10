package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security

import jakarta.servlet.http.Cookie
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.fullmakt.FullmaktClient
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.fullmakt.FullmaktException
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.fullmakt.RepresentasjonsforholdValidity
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.person.PersonService
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.person.pdl.PdlAdressebeskyttelsesgradering
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.skjerming.SkjermingClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class AuthorizationService(
    @Value("\${strengt-fortrolig-tilgang.group.id}") private val strengtFortroligAdresseGroupId: String,
    @Value("\${fortrolig-tilgang.group.id}") private val fortroligAdresseGroupId: String,
    @Value("\${skjermet-tilgang.group.id}") private val skjermetGroupId: String,
    @Value("\${pensjon-saksbehandler-tilgang.group.id}") private val pensjonSaksbehandlerGroupId: String,
    @Value("\${pensjon-veileder-tilgang.group.id}") private val pensjonVeilederGroupId: String,
    @Value("\${pensjon-brukerhjelpa-tilgang.group.id}") private val pensjonBrukerhjelpaGroupId: String,
    @Value("\${pensjon-okonomi-tilgang.group.id}") private val pensjonOkonmiGroupId: String,
    private val tokenService: TokenService,
    private val skjermingClient: SkjermingClient,
    private val personService: PersonService,
    private val fullmaktClient: FullmaktClient
) {

    private val log: Logger = LoggerFactory.getLogger(AuthorizationService::class.java)

    fun checkVeilederTilgangTilInnbygger(pid: String) {
        checkBasisTilgang()
        checkSkjermetAnsatt(pid)
        checkAdressebeskyttetInnbygger(pid)
    }

    fun checkBorgerTilgang(navOnBehalfOfCookie: Cookie?) : AuthenticatedUserDetails {
        val requestingPid = tokenService.determineRequestingPid()
        if (isFullmaktsCase(navOnBehalfOfCookie, requestingPid)) {
            val fullmaktsgiverPid = navOnBehalfOfCookie!!.value
            haandterFullmakt(fullmaktsgiverPid, requestingPid)
            return AuthenticatedUserDetails(fullmaktsgiverPid, true)
        }else {
            checkAdressebeskyttelseAndLoginLevel(requestingPid)
            return AuthenticatedUserDetails(requestingPid, false)
        }
    }

    private fun checkBasisTilgang() {
        val adGroups = tokenService.getGroups()

        if (!(adGroups.contains(pensjonSaksbehandlerGroupId) ||
            adGroups.contains(pensjonVeilederGroupId) ||
            adGroups.contains(pensjonBrukerhjelpaGroupId) ||
            adGroups.contains(pensjonOkonmiGroupId))) {
            log.info("Veileder/saksbehandler mangler basis rolle for pensjon. Nekter tilgang.")
            throw VeilederUnauthorizedException()
        }
    }

    private fun checkSkjermetAnsatt(pid: String) {
        if (!tokenService.getGroups().contains(skjermetGroupId) && skjermingClient.isSkjermet(pid)) {
            log.info("Bruker skjermet, veileder/saksbehandler mangler autorisering. Nekter tilgang.")
            throw VeilederUnauthorizedException()
        }
    }

    private fun checkAdressebeskyttetInnbygger(pid: String) {
        val adressebeskyttelse = personService.getAdressebeskyttelsesgrad(pid)
        val adGroups = tokenService.getGroups()
        when (adressebeskyttelse) {
            PdlAdressebeskyttelsesgradering.FORTROLIG -> {
                if (!adGroups.contains(fortroligAdresseGroupId)) {
                    log.info("Bruker adressebeskyttet - fortrolig, veileder/saksbehandler mangler autorisering. Nekter tilgang.")
                    throw VeilederUnauthorizedException()
                }
            }
            PdlAdressebeskyttelsesgradering.STRENGT_FORTROLIG -> {
                if (!adGroups.contains(strengtFortroligAdresseGroupId)) {
                    log.info("Bruker adressebeskyttet - strengt fortrolig, veileder/saksbehandler mangler autorisering. Nekter tilgang.")
                    throw VeilederUnauthorizedException()
                }
            }
            PdlAdressebeskyttelsesgradering.STRENGT_FORTROLIG_UTLAND -> {
                if (!adGroups.contains(strengtFortroligAdresseGroupId)) {
                    log.info("Bruker adressebeskyttet - strengt fortrolig utland , veileder/saksbehandler mangler autorisering. Nekter tilgang.")
                    throw VeilederUnauthorizedException()
                }
            }
            else -> {}
        }
    }

    private fun checkAdressebeskyttelseAndLoginLevel(requestingPid: String) {
        if (!tokenService.isLoginLevelHigh()) {
            val adressebeskyttelse = personService.getAdressebeskyttelsesgrad(requestingPid)
            if (adressebeskyttelse == PdlAdressebeskyttelsesgradering.STRENGT_FORTROLIG || adressebeskyttelse == PdlAdressebeskyttelsesgradering.STRENGT_FORTROLIG_UTLAND) {
                log.info("Bruker adressebeskyttet - Strengt Fortrolig, innloggingsnivå for lavt. Nekter adgang")
                throw LoginLevelTooLowException()
            }
        }
    }

    private fun isFullmaktsCase(navOnBehalfOfCookie: Cookie?, requestingPid: String): Boolean {
        if (navOnBehalfOfCookie != null) {
            log.info("Cookie'en nav-obo er satt og det antyder fullmaktscenario")
            val fullmaktsgiverPid = navOnBehalfOfCookie.value
            if (requestingPid != "" && requestingPid != fullmaktsgiverPid) {
                return true
            }
        }
        return false
    }

    private fun haandterFullmakt(fullmaktsgiverPid: String, requestingPid: String): RepresentasjonsforholdValidity {
        try {
            val harGyldigFullmakt = fullmaktClient.hasValidRepresentasjonsforhold(fullmaktsgiverPid, requestingPid)
            if (harGyldigFullmakt == null || !harGyldigFullmakt.hasValidRepresentasjonsforhold) {
                log.info("Fullmaktsforhold er ikke funnet. Nekter adgang")
                throw NoFullmaktPresentException()
            }

            if(personService.hasAdressebeskyttelse(fullmaktsgiverPid)) {
                log.info("Fullmaktsforhold for bruker med adressebeskyttelse. Nekter adgang")
                throw NoFullmaktPresentException()
            }

            return harGyldigFullmakt
        } catch (e: FullmaktException) {
            log.error("Noe gikk galt ved kall til fullmakt. Nekter adgang")
            log.warn("FullmaktException: ${e.message}")
            throw NoFullmaktPresentException()
        }
    }
}