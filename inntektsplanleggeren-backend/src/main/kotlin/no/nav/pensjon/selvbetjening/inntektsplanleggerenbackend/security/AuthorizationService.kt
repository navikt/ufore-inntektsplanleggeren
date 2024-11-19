package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security

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
    @Value("\${pensjon-klagebeh-tilgang.group.id}") private val pensjonKlagebehandlerGroupId: String,
    @Value("\${pensjon-ufoere-tilgang.group.id}") private val pensjonUfoereGroupId: String,
    private val tokenService: TokenService,
    private val skjermingClient: SkjermingClient,
    private val personService: PersonService
) {

    private val log: Logger = LoggerFactory.getLogger(AuthorizationService::class.java)

    fun checkVeilederTilgangTilInnbygger(pid: String) {
        checkBasisTilgang()
        checkSkjermetAnsatt(pid)
        checkAdressebeskyttetInnbygger(pid)
    }

    private fun checkBasisTilgang() {
        val adGroups = tokenService.getGroups()

        if (!adGroups.contains(pensjonUfoereGroupId)) {
            log.info("Veileder/saksbehandler mangler basis rolle for ufore. Nekter tilgang.")
            throw VeilederUnauthorizedException()
        }
        if (adGroups.contains(pensjonSaksbehandlerGroupId) ||
            adGroups.contains(pensjonVeilederGroupId) ||
            adGroups.contains(pensjonBrukerhjelpaGroupId) ||
            adGroups.contains(pensjonKlagebehandlerGroupId)) {
            return
        } else {
            log.info("Veileder/saksbehandler mangler basis rolle for pensjon. Nekter tilgang.")
            throw VeilederUnauthorizedException()
        }
    }

    private fun checkSkjermetAnsatt(pid: String) {
        if (skjermingClient.isSkjermet(pid) && !tokenService.getGroups().contains(skjermetGroupId)) {
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
}