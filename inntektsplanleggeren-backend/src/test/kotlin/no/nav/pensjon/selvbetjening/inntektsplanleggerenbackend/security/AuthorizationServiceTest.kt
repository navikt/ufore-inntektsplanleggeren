package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security

import io.micrometer.core.instrument.Metrics
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import jakarta.servlet.http.Cookie
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.fullmakt.FullmaktException
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.fullmakt.OBO_TILGANG_EVENT
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.fullmakt.OboTilgangOutcome
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.fullmakt.RepresentasjonClient
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.fullmakt.RepresentasjonsforholdValidity
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.person.PersonService
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.person.pdl.PdlAdressebeskyttelsesgradering
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.skjerming.SkjermingClient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import kotlin.test.assertEquals

class AuthorizationServiceTest {

    private val strengtFortroligAdresseGroupId = "strengfortrolig"
    private val fortroligAdresseGroupId = "fortrolig"
    private val skjermetGroupId = "skjermet"
    private val pensjonSaksbehandlerGroupId = "saksbehandler"
    private val pensjonVeilederGroupId = "veileder"
    private val pensjonBrukerHjelpa = "brukerhjelpa"
    private val pensjonOkonomiGroupId = "okonomi"

    private val VeilederUnauthorizedExceptionName = "no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security.VeilederUnauthorizedException"

    private val tokenService = mock(TokenService::class.java)
    private val skjermingClient = mock(SkjermingClient::class.java)
    private val personService = mock(PersonService::class.java)
    private val representasjonClient = mock(RepresentasjonClient::class.java)

    private lateinit var meterRegistry: SimpleMeterRegistry

    private val authorizationService = AuthorizationService(
        strengtFortroligAdresseGroupId,
        fortroligAdresseGroupId,
        skjermetGroupId,
        pensjonSaksbehandlerGroupId,
        pensjonVeilederGroupId,
        pensjonBrukerHjelpa,
        pensjonOkonomiGroupId,
        tokenService,
        skjermingClient,
        personService,
        representasjonClient
    )

    @BeforeEach
    fun setupMetrics() {
        reset(tokenService, skjermingClient, personService, representasjonClient)
        meterRegistry = SimpleMeterRegistry()
        Metrics.addRegistry(meterRegistry)
    }

    @AfterEach
    fun cleanupMetrics() {
        Metrics.removeRegistry(meterRegistry)
        meterRegistry.clear()
    }

    private fun counterValue(outcome: OboTilgangOutcome, method: String = "GET") =
        meterRegistry.find(OBO_TILGANG_EVENT)
            .tag("outcome", outcome.tag)
            .tag("method", method)
            .counter()
            ?.count() ?: 0.0

    //---------------------------
    // -- Veileder/saksbehandler
    //----------------------------
    //- Basic access
    //---------------
    @Test
    fun `should return nothing when veileder access to innbygger without addressebeskyttelse or skjerming`() {
        val pid = "12345678901"
        `when` (tokenService.getGroups()).thenReturn(listOf(pensjonVeilederGroupId))
        `when` (skjermingClient.isSkjermet(pid)).thenReturn(false)
        `when` (personService.getAdressebeskyttelsesgrad(pid)).thenReturn(null)
        authorizationService.checkVeilederTilgangTilInnbygger(pid)
    }

    @Test
    fun `should return nothing when veileder access to innbygger with addressebeskyttelse ugradert and no skjerming`() {
        val pid = "12345678901"
        `when` (tokenService.getGroups()).thenReturn(listOf(pensjonVeilederGroupId))
        `when` (skjermingClient.isSkjermet(pid)).thenReturn(false)
        `when` (personService.getAdressebeskyttelsesgrad(pid)).thenReturn(PdlAdressebeskyttelsesgradering.UGRADERT)
        authorizationService.checkVeilederTilgangTilInnbygger(pid)
    }

    @Test
    fun `should return nothing when saksbehandler access to innbygger with addressebeskyttelse ugradert and no skjerming`() {
        val pid = "12345678901"
        `when` (tokenService.getGroups()).thenReturn(listOf(pensjonSaksbehandlerGroupId))
        `when` (skjermingClient.isSkjermet(pid)).thenReturn(false)
        `when` (personService.getAdressebeskyttelsesgrad(pid)).thenReturn(PdlAdressebeskyttelsesgradering.UGRADERT)
        authorizationService.checkVeilederTilgangTilInnbygger(pid)
    }

    @Test
    fun `should return nothing when brukerhjelpa access to innbygger with addressebeskyttelse ugradert and no skjerming`() {
        val pid = "12345678901"
        `when` (tokenService.getGroups()).thenReturn(listOf(pensjonBrukerHjelpa))
        `when` (skjermingClient.isSkjermet(pid)).thenReturn(false)
        `when` (personService.getAdressebeskyttelsesgrad(pid)).thenReturn(PdlAdressebeskyttelsesgradering.UGRADERT)
        authorizationService.checkVeilederTilgangTilInnbygger(pid)
    }

    @Test
    fun `should return nothing when okonomi access to innbygger with addressebeskyttelse ugradert and no skjerming`() {
        val pid = "12345678901"
        `when` (tokenService.getGroups()).thenReturn(listOf(pensjonOkonomiGroupId))
        `when` (skjermingClient.isSkjermet(pid)).thenReturn(false)
        `when` (personService.getAdressebeskyttelsesgrad(pid)).thenReturn(PdlAdressebeskyttelsesgradering.UGRADERT)
        authorizationService.checkVeilederTilgangTilInnbygger(pid)
    }

    @Test
    fun `should return Exception when nav ansatt med skjermet and no pensjon rolle access to innbygger with addressebeskyttelse ugradert and no skjerming`() {
        val pid = "12345678901"
        `when` (tokenService.getGroups()).thenReturn(listOf(skjermetGroupId))
        val exception = assertThrows<VeilederUnauthorizedException> { authorizationService.checkVeilederTilgangTilInnbygger(pid) }
        assertEquals(VeilederUnauthorizedExceptionName,exception.toString())
    }

    //---------------
    //- Skjerming
    //---------------

    @Test
    fun `should return nothing when veileder with access to skjermede access innbygger with skjerming`() {
        val pid = "12345678901"
        `when` (tokenService.getGroups()).thenReturn(listOf(pensjonVeilederGroupId,skjermetGroupId))
        `when` (skjermingClient.isSkjermet(pid)).thenReturn(true)
        `when` (personService.getAdressebeskyttelsesgrad(pid)).thenReturn(null)
        authorizationService.checkVeilederTilgangTilInnbygger(pid)
    }

    @Test
    fun `should return Exception when veileder without access to skjermede access innbygger with skjerming`() {
        val pid = "12345678901"
        `when` (tokenService.getGroups()).thenReturn(listOf(pensjonVeilederGroupId))
        `when` (skjermingClient.isSkjermet(pid)).thenReturn(true)
        `when` (personService.getAdressebeskyttelsesgrad(pid)).thenReturn(null)
        val exception = assertThrows<VeilederUnauthorizedException> { authorizationService.checkVeilederTilgangTilInnbygger(pid) }
        assertEquals(VeilederUnauthorizedExceptionName,exception.toString())
    }

    //---------------------
    //- Adressebeskyttelse
    //---------------------

    @Test
    fun `should return nothing when veileder with access to strengt fortrolig adresse access innbygger with strengt fortrolig adresse`() {
        val pid = "12345678901"
        `when` (tokenService.getGroups()).thenReturn(listOf(pensjonVeilederGroupId,strengtFortroligAdresseGroupId))
        `when` (skjermingClient.isSkjermet(pid)).thenReturn(false)
        `when` (personService.getAdressebeskyttelsesgrad(pid)).thenReturn(PdlAdressebeskyttelsesgradering.STRENGT_FORTROLIG)
        authorizationService.checkVeilederTilgangTilInnbygger(pid)
    }

    @Test
    fun `should return nothing when veileder with access to strengt fortrolig adresse access innbygger with strengt fortrolig adresse utland`() {
        val pid = "12345678901"
        `when` (tokenService.getGroups()).thenReturn(listOf(pensjonVeilederGroupId,strengtFortroligAdresseGroupId))
        `when` (skjermingClient.isSkjermet(pid)).thenReturn(false)
        `when` (personService.getAdressebeskyttelsesgrad(pid)).thenReturn(PdlAdressebeskyttelsesgradering.STRENGT_FORTROLIG_UTLAND)
        authorizationService.checkVeilederTilgangTilInnbygger(pid)
    }

    @Test
    fun `should return nothing when veileder with access to fortrolig adresse access innbygger with fortrolig adresse`() {
        val pid = "12345678901"
        `when` (tokenService.getGroups()).thenReturn(listOf(pensjonVeilederGroupId,fortroligAdresseGroupId))
        `when` (skjermingClient.isSkjermet(pid)).thenReturn(false)
        `when` (personService.getAdressebeskyttelsesgrad(pid)).thenReturn(PdlAdressebeskyttelsesgradering.FORTROLIG)
        authorizationService.checkVeilederTilgangTilInnbygger(pid)
    }

    @Test
    fun `should return nothing when veileder with access to strengt fortrolig adresse access innbygger without adressebeskyttelse - ugradert`() {
        val pid = "12345678901"
        `when` (tokenService.getGroups()).thenReturn(listOf(pensjonVeilederGroupId,strengtFortroligAdresseGroupId))
        `when` (skjermingClient.isSkjermet(pid)).thenReturn(false)
        `when` (personService.getAdressebeskyttelsesgrad(pid)).thenReturn(PdlAdressebeskyttelsesgradering.UGRADERT)
        authorizationService.checkVeilederTilgangTilInnbygger(pid)
    }

    @Test
    fun `should return Exception when veileder with access to strengt fortrolig adresse access innbygger with fortrolig adresse`() {
        val pid = "12345678901"
        `when` (tokenService.getGroups()).thenReturn(listOf(pensjonVeilederGroupId,strengtFortroligAdresseGroupId))
        `when` (skjermingClient.isSkjermet(pid)).thenReturn(false)
        `when` (personService.getAdressebeskyttelsesgrad(pid)).thenReturn(PdlAdressebeskyttelsesgradering.FORTROLIG)
        val exception = assertThrows<VeilederUnauthorizedException> { authorizationService.checkVeilederTilgangTilInnbygger(pid) }
        assertEquals(VeilederUnauthorizedExceptionName,exception.toString())
    }

    @Test
    fun `should return Exception when veileder with access to fortrolig adresse access innbygger with strengt fortrolig adresse`() {
        val pid = "12345678901"
        `when` (tokenService.getGroups()).thenReturn(listOf(pensjonVeilederGroupId,fortroligAdresseGroupId))
        `when` (skjermingClient.isSkjermet(pid)).thenReturn(false)
        `when` (personService.getAdressebeskyttelsesgrad(pid)).thenReturn(PdlAdressebeskyttelsesgradering.STRENGT_FORTROLIG)
        val exception = assertThrows<VeilederUnauthorizedException> { authorizationService.checkVeilederTilgangTilInnbygger(pid) }
        assertEquals(VeilederUnauthorizedExceptionName,exception.toString())
    }

    @Test
    fun `should return Exception when veileder with no access to addressebekyttede access innbygger with strengt fortrolig adresse`() {
        val pid = "12345678901"
        `when` (tokenService.getGroups()).thenReturn(listOf(pensjonVeilederGroupId))
        `when` (skjermingClient.isSkjermet(pid)).thenReturn(false)
        `when` (personService.getAdressebeskyttelsesgrad(pid)).thenReturn(PdlAdressebeskyttelsesgradering.STRENGT_FORTROLIG)
        val exception = assertThrows<VeilederUnauthorizedException> { authorizationService.checkVeilederTilgangTilInnbygger(pid) }
        assertEquals(VeilederUnauthorizedExceptionName,exception.toString())
    }

    @Test
    fun `should return Exception when veileder with no access to addressebekyttede access innbygger with strengt fortrolig adresse utland`() {
        val pid = "12345678901"
        `when` (tokenService.getGroups()).thenReturn(listOf(pensjonVeilederGroupId))
        `when` (skjermingClient.isSkjermet(pid)).thenReturn(false)
        `when` (personService.getAdressebeskyttelsesgrad(pid)).thenReturn(PdlAdressebeskyttelsesgradering.STRENGT_FORTROLIG_UTLAND)
        val exception = assertThrows<VeilederUnauthorizedException> { authorizationService.checkVeilederTilgangTilInnbygger(pid) }
        assertEquals(VeilederUnauthorizedExceptionName,exception.toString())
    }

    @Test
    fun `should return Exception when veileder with no access to addressebekyttede access innbygger with fortrolig adresse`() {
        val pid = "12345678901"
        `when` (tokenService.getGroups()).thenReturn(listOf(pensjonVeilederGroupId))
        `when` (skjermingClient.isSkjermet(pid)).thenReturn(false)
        `when` (personService.getAdressebeskyttelsesgrad(pid)).thenReturn(PdlAdressebeskyttelsesgradering.FORTROLIG)
        val exception = assertThrows<VeilederUnauthorizedException> { authorizationService.checkVeilederTilgangTilInnbygger(pid) }
        assertEquals(VeilederUnauthorizedExceptionName,exception.toString())
    }

    //---------------------------
    // -- Borger
    //----------------------------
    // -- Normal - ingen fullmakt
    //----------------------------
    @Test
    fun `should return pid and no isFullmakt=false when borger with no addressebekyttelse access himself login level high`() {
        val pid = "12345678901"
        `when` (tokenService.determineRequestingPid()).thenReturn(pid)
        `when` (tokenService.isLoginLevelHigh()).thenReturn(true)
        `when` (personService.hasAdressebeskyttelse(pid)).thenReturn(false)
        val authenticatedUserDetails = authorizationService.checkBorgerTilgang("GET", null)
        assertEquals(pid,authenticatedUserDetails.pid)
        assertEquals(false,authenticatedUserDetails.isFullmakt)
    }

    @Test
    fun `should return pid and no isFullmakt=false when borger with no addressebekyttelse access himself login level substantial`() {
        val pid = "12345678901"
        `when` (tokenService.determineRequestingPid()).thenReturn(pid)
        `when` (tokenService.isLoginLevelHigh()).thenReturn(false)
        `when` (personService.hasAdressebeskyttelse(pid)).thenReturn(false)
        val authenticatedUserDetails = authorizationService.checkBorgerTilgang("GET", null)
        `when` (personService.getAdressebeskyttelsesgrad(pid)).thenReturn(null)
        assertEquals(pid,authenticatedUserDetails.pid)
        assertEquals(false,authenticatedUserDetails.isFullmakt)
    }

    @Test
    fun `should return pid and no isFullmakt=false when borger with addressebekyttelse access himself login level high`() {
        val pid = "12345678901"
        `when` (tokenService.determineRequestingPid()).thenReturn(pid)
        `when` (tokenService.isLoginLevelHigh()).thenReturn(true)
        `when` (personService.hasAdressebeskyttelse(pid)).thenReturn(true)
        val authenticatedUserDetails = authorizationService.checkBorgerTilgang("GET", null)
        assertEquals(pid,authenticatedUserDetails.pid)
        assertEquals(false,authenticatedUserDetails.isFullmakt)
    }

    @Test
    fun `should return Exception when borger with addressebekyttelse Strengt fortrolig access himself login level substantial`() {
        val pid = "12345678901"
        `when` (tokenService.determineRequestingPid()).thenReturn(pid)
        `when` (tokenService.isLoginLevelHigh()).thenReturn(false)
        `when` (personService.getAdressebeskyttelsesgrad(pid)).thenReturn(PdlAdressebeskyttelsesgradering.STRENGT_FORTROLIG)
        assertThrows<LoginLevelTooLowException> { authorizationService.checkBorgerTilgang("GET", null) }
    }

    @Test
    fun `should return Exception when borger with addressebekyttelse Strengt fortrolig utland access himself login level substantial`() {
        val pid = "12345678901"
        `when` (tokenService.determineRequestingPid()).thenReturn(pid)
        `when` (tokenService.isLoginLevelHigh()).thenReturn(false)
        `when` (personService.getAdressebeskyttelsesgrad(pid)).thenReturn(PdlAdressebeskyttelsesgradering.STRENGT_FORTROLIG_UTLAND)
        assertThrows<LoginLevelTooLowException> { authorizationService.checkBorgerTilgang("GET", null) }
    }

    @Test
    fun `should return pid and isFullmakt=false when borger with addressebekyttelse Strengt fortrolig access himself login level high`() {
        val pid = "12345678901"
        `when` (tokenService.determineRequestingPid()).thenReturn(pid)
        `when` (tokenService.isLoginLevelHigh()).thenReturn(true)
 //       `when` (personService.getAdressebeskyttelsesgrad(pid)).thenReturn(PdlAdressebeskyttelsesgradering.STRENGT_FORTROLIG_UTLAND)
        val authenticatedUserDetails = authorizationService.checkBorgerTilgang("GET", null)
        assertEquals(pid,authenticatedUserDetails.pid)
        assertEquals(false,authenticatedUserDetails.isFullmakt)
    }

    @Test
    fun `should return pid and isFullmakt=false when borger with addressebekyttelse Fortrolig access himself login level substantial`() {
        val pid = "12345678901"
        `when` (tokenService.determineRequestingPid()).thenReturn(pid)
        `when` (tokenService.isLoginLevelHigh()).thenReturn(false)
        val authenticatedUserDetails = authorizationService.checkBorgerTilgang("GET", null)
        assertEquals(pid,authenticatedUserDetails.pid)
        assertEquals(false,authenticatedUserDetails.isFullmakt)
    }

    //----------------------------
    // -- fullmakt
    //----------------------------
    @Test
    fun `should return fullmaktsgiver pid and no isFullmakt=true when borger with no addressebekyttelse is accessed by fullmaktshaver med gyldig fullmakt`() {
        val subjectPid = "12345678901"
        val resourcePid = "12345678905"
        val resourcePidKryptert = "fnr_kryptert"
        val navOnBehalfOfCCookie = Cookie("navOnBehalfOfCookie", resourcePidKryptert)
        val httpMethod = "GET"
        `when` (tokenService.determineRequestingPid()).thenReturn(subjectPid)
        `when` (representasjonClient.hasValidRepresentasjonsforhold(httpMethod, resourcePidKryptert, subjectPid)).thenReturn(RepresentasjonsforholdValidity(true,"Ole Brum", resourcePidKryptert, resourcePid))
        `when` (personService.hasAdressebeskyttelse(resourcePid)).thenReturn(false)
        val authenticatedUserDetails = authorizationService.checkBorgerTilgang(httpMethod, navOnBehalfOfCCookie)
        assertEquals(resourcePid,authenticatedUserDetails.pid)
        assertEquals(true,authenticatedUserDetails.isFullmakt)
        assertEquals(1.0, counterValue(OboTilgangOutcome.INNVILGET))
    }

    @Test
    fun `should return Exception when borger with addressebekyttelse is accessed by fullmaktshaver med gyldig fullmakt`() {
        val subjectPid = "12345678901"
        val resourcePid = "12345678905"
        val resourcePidKryptert = "fnr_kryptert"
        val navOnBehalfOfCCookie = Cookie("navOnBehalfOfCookie", resourcePidKryptert)
        val httpMethod = "GET"
        `when` (tokenService.determineRequestingPid()).thenReturn(subjectPid)
        `when` (representasjonClient.hasValidRepresentasjonsforhold(httpMethod, resourcePidKryptert, subjectPid)).thenReturn(RepresentasjonsforholdValidity(true,"Ole Brum", resourcePidKryptert, resourcePid))
        `when` (personService.hasAdressebeskyttelse(resourcePid)).thenReturn(true)
        assertThrows<NoFullmaktPresentException> { authorizationService.checkBorgerTilgang(httpMethod, navOnBehalfOfCCookie) }
        assertEquals(1.0, counterValue(OboTilgangOutcome.ADRESSEBESKYTTELSE))
    }

    @Test
    fun `should return Exception when borger with no addressebekyttelse is accessed by fullmaktshaver uten gyldig fullmakt`() {
        val subjectPid = "12345678901"
        val resourcePid = "12345678905"
        val resourcePidKryptert = "fnr_kryptert"
        val navOnBehalfOfCCookie = Cookie("navOnBehalfOfCookie", resourcePidKryptert)
        val httpMethod = "GET"
        `when` (tokenService.determineRequestingPid()).thenReturn(subjectPid)
        `when` (representasjonClient.hasValidRepresentasjonsforhold(httpMethod, resourcePidKryptert, subjectPid)).thenReturn(RepresentasjonsforholdValidity(false,null, resourcePidKryptert, resourcePid))
        `when` (personService.hasAdressebeskyttelse(resourcePid)).thenReturn(false)
        assertThrows<NoFullmaktPresentException> { authorizationService.checkBorgerTilgang(httpMethod, navOnBehalfOfCCookie) }
        assertEquals(1.0, counterValue(OboTilgangOutcome.INGEN_GYLDIG_REPRESENTASJON))
    }

    @Test
    fun `should return Exception and count fullmakt feil when representasjon client fails`() {
        val subjectPid = "12345678901"
        val resourcePidKryptert = "fnr_kryptert"
        val navOnBehalfOfCCookie = Cookie("navOnBehalfOfCookie", resourcePidKryptert)
        val httpMethod = "POST"
        `when` (tokenService.determineRequestingPid()).thenReturn(subjectPid)
        `when` (representasjonClient.hasValidRepresentasjonsforhold(httpMethod, resourcePidKryptert, subjectPid))
            .thenThrow(FullmaktException("representasjon", "feil"))

        assertThrows<NoFullmaktPresentException> { authorizationService.checkBorgerTilgang(httpMethod, navOnBehalfOfCCookie) }
        assertEquals(1.0, counterValue(OboTilgangOutcome.FULLMAKT_FEIL, "POST"))
    }
}
