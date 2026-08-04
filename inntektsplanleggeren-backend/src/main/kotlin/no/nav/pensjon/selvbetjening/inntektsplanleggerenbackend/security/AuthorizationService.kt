package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security

import jakarta.servlet.http.Cookie
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.fullmakt.EVENT_OBO_AVVIST
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.fullmakt.FullmaktException
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.fullmakt.OboTilgangOutcome
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.fullmakt.RepresentasjonClient
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.fullmakt.RepresentasjonsforholdValidity
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.fullmakt.countOboTilgang
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.fullmakt.requiredRepresentasjonstyper
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
    private val representasjonClient: RepresentasjonClient
) {

    private val log: Logger = LoggerFactory.getLogger(AuthorizationService::class.java)

    fun checkVeilederTilgangTilInnbygger(pid: String) {
        checkBasisTilgang()
        checkSkjermetAnsatt(pid)
        checkAdressebeskyttetInnbygger(pid)
    }

    fun checkBorgerTilgang(httpMethod: String, navOnBehalfOfCookie: Cookie?) : AuthenticatedUserDetails {
        val requestingPid = tokenService.determineRequestingPid()
        if (navOnBehalfOfCookie != null) {
            val representertPidKryptert = navOnBehalfOfCookie.value
            val representasjonsforhold = haandterFullmakt(httpMethod, representertPidKryptert, requestingPid)
            if (representasjonsforhold.representertPid != requestingPid) {
                countOboTilgang(OboTilgangOutcome.INNVILGET, httpMethod)
                return AuthenticatedUserDetails(representasjonsforhold.representertPid, true)
            }
        }

        checkAdressebeskyttelseAndLoginLevel(requestingPid)
        return AuthenticatedUserDetails(requestingPid, false)
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

    private fun haandterFullmakt(httpMethod: String, representertPid: String, requestingPid: String): RepresentasjonsforholdValidity {
        val requiredRepresentasjonstyper = requiredRepresentasjonstyper(httpMethod)
        try {
            val harGyldigFullmakt = representasjonClient.hasValidRepresentasjonsforhold(httpMethod, representertPid, requestingPid)
            if (harGyldigFullmakt == null || !harGyldigFullmakt.hasValidRepresentasjonsforhold) {
                countOboTilgang(OboTilgangOutcome.INGEN_GYLDIG_REPRESENTASJON, httpMethod)
                log.warn(
                    "Fullmaktsforhold er ikke funnet. Nekter adgang",
                    kv("event", EVENT_OBO_AVVIST),
                    kv("obo_outcome", OboTilgangOutcome.INGEN_GYLDIG_REPRESENTASJON.tag),
                    kv("obo_method", httpMethod),
                    kv("obo_paakrevde_typer", requiredRepresentasjonstyper.joinToString(",")),
                    kv("obo_tomt_svar", harGyldigFullmakt == null)
                )
                throw NoFullmaktPresentException()
            }

            if(personService.hasAdressebeskyttelse(harGyldigFullmakt.representertPid)) {
                countOboTilgang(OboTilgangOutcome.ADRESSEBESKYTTELSE, httpMethod)
                log.warn(
                    "Fullmaktsforhold for bruker med adressebeskyttelse. Nekter adgang",
                    kv("event", EVENT_OBO_AVVIST),
                    kv("obo_outcome", OboTilgangOutcome.ADRESSEBESKYTTELSE.tag),
                    kv("obo_method", httpMethod),
                    kv("obo_paakrevde_typer", requiredRepresentasjonstyper.joinToString(","))
                )
                throw NoFullmaktPresentException()
            }

            return harGyldigFullmakt
        } catch (e: FullmaktException) {
            countOboTilgang(OboTilgangOutcome.FULLMAKT_FEIL, httpMethod)
            log.error(
                "Noe gikk galt ved kall til fullmakt. Nekter adgang",
                kv("event", EVENT_OBO_AVVIST),
                kv("obo_outcome", OboTilgangOutcome.FULLMAKT_FEIL.tag),
                kv("obo_method", httpMethod),
                kv("obo_paakrevde_typer", requiredRepresentasjonstyper.joinToString(",")),
                kv("obo_feilmelding", e.message)
            )
            throw NoFullmaktPresentException()
        }
    }
}