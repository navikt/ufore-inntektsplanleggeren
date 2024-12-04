package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security

import jakarta.servlet.http.Cookie
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.fullmakt.FullmaktClient
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.fullmakt.RepresentasjonsforholdValidity
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.person.PersonService
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.person.pdl.PdlAdressebeskyttelsesgradering
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.skjerming.SkjermingClient
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
    private val fullmaktClient = mock(FullmaktClient::class.java)

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
        fullmaktClient
    )
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
        val authenticatedUserDetails = authorizationService.checkBorgerTilgang(null)
        assertEquals(pid,authenticatedUserDetails.pid)
        assertEquals(false,authenticatedUserDetails.isFullmakt)
    }

    @Test
    fun `should return pid and no isFullmakt=false when borger with no addressebekyttelse access himself login level substantial`() {
        val pid = "12345678901"
        `when` (tokenService.determineRequestingPid()).thenReturn(pid)
        `when` (tokenService.isLoginLevelHigh()).thenReturn(false)
        `when` (personService.hasAdressebeskyttelse(pid)).thenReturn(false)
        val authenticatedUserDetails = authorizationService.checkBorgerTilgang(null)
        assertEquals(pid,authenticatedUserDetails.pid)
        assertEquals(false,authenticatedUserDetails.isFullmakt)
    }

    @Test
    fun `should return pid and no isFullmakt=false when borger with addressebekyttelse access himself login level high`() {
        val pid = "12345678901"
        `when` (tokenService.determineRequestingPid()).thenReturn(pid)
        `when` (tokenService.isLoginLevelHigh()).thenReturn(true)
        `when` (personService.hasAdressebeskyttelse(pid)).thenReturn(true)
        val authenticatedUserDetails = authorizationService.checkBorgerTilgang(null)
        assertEquals(pid,authenticatedUserDetails.pid)
        assertEquals(false,authenticatedUserDetails.isFullmakt)
    }

    @Test
    fun `should return Exception when borger withaddressebekyttelse access himself login level substantial`() {
        val pid = "12345678901"
        `when` (tokenService.determineRequestingPid()).thenReturn(pid)
        `when` (tokenService.isLoginLevelHigh()).thenReturn(false)
        `when` (personService.hasAdressebeskyttelse(pid)).thenReturn(true)
        assertThrows<LoginLevelTooLowException> { authorizationService.checkBorgerTilgang(null) }
    }

    //----------------------------
    // -- fullmakt
    //----------------------------
    @Test
    fun `should return fullmaktsgiver pid and no isFullmakt=true when borger with no addressebekyttelse is accessed by fullmaktshaver med gyldig fullmakt`() {
        val subjectPid = "12345678901"
        val resourcePid = "12345678905"
        val navOnBehalfOfCCookie = Cookie("navOnBehalfOfCookie",resourcePid)
        `when` (tokenService.determineRequestingPid()).thenReturn(subjectPid)
        `when` (fullmaktClient.hasValidRepresentasjonsforhold(resourcePid, subjectPid)).thenReturn(RepresentasjonsforholdValidity(true,"Ole Brum"))
        `when` (personService.hasAdressebeskyttelse(resourcePid)).thenReturn(false)
        val authenticatedUserDetails = authorizationService.checkBorgerTilgang(navOnBehalfOfCCookie)
        assertEquals(resourcePid,authenticatedUserDetails.pid)
        assertEquals(true,authenticatedUserDetails.isFullmakt)
    }

    @Test
    fun `should return Exception when borger with addressebekyttelse is accessed by fullmaktshaver med gyldig fullmakt`() {
        val subjectPid = "12345678901"
        val resourcePid = "12345678905"
        val navOnBehalfOfCCookie = Cookie("navOnBehalfOfCookie",resourcePid)
        `when` (tokenService.determineRequestingPid()).thenReturn(subjectPid)
        `when` (fullmaktClient.hasValidRepresentasjonsforhold(resourcePid, subjectPid)).thenReturn(RepresentasjonsforholdValidity(true,"Ole Brum"))
        `when` (personService.hasAdressebeskyttelse(resourcePid)).thenReturn(true)
        assertThrows<NoFullmaktPresentException> { authorizationService.checkBorgerTilgang(navOnBehalfOfCCookie) }
    }

    @Test
    fun `should return Exception when borger with no addressebekyttelse is accessed by fullmaktshaver uten gyldig fullmakt`() {
        val subjectPid = "12345678901"
        val resourcePid = "12345678905"
        val navOnBehalfOfCCookie = Cookie("navOnBehalfOfCookie",resourcePid)
        `when` (tokenService.determineRequestingPid()).thenReturn(subjectPid)
        `when` (fullmaktClient.hasValidRepresentasjonsforhold(resourcePid, subjectPid)).thenReturn(RepresentasjonsforholdValidity(false,null))
        `when` (personService.hasAdressebeskyttelse(resourcePid)).thenReturn(false)
        assertThrows<NoFullmaktPresentException> { authorizationService.checkBorgerTilgang(navOnBehalfOfCCookie) }
    }
}