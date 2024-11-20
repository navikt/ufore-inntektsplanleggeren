package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.person.PersonService
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.person.pdl.PdlAdressebeskyttelsesgradering
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.skjerming.SkjermingClient
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.*
import kotlin.test.assertEquals


class AuthorizationServiceTest {

    private val strengtFortroligAdresseGroupId = "strengfortrolig"
    private val fortroligAdresseGroupId = "fortrolig"
    private val skjermetGroupId = "skjermet"
    private val pensjonSaksbehandlerGroupId = "saksbehandler"
    private val pensjonVeilederGroupId = "veileder"
    private val pensjonBrukerHjelpa = "brukerhjelpa"
    private val pensjonKlageBehandlerGroupId = "klagebehandler"
    private val pensjonUfoereGroupId = "ufore"
    private val pensjonOkonomiGroupId = "okonomi"

    private val VeilederUnauthorizedExceptionName = "no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security.VeilederUnauthorizedException"

    private val tokenService = Mockito.mock(TokenService::class.java)
    private val skjermingClient = Mockito.mock(SkjermingClient::class.java)
    private val personService = Mockito.mock(PersonService::class.java)

    private val authorizationService = AuthorizationService(
        strengtFortroligAdresseGroupId,
        fortroligAdresseGroupId,
        skjermetGroupId,
        pensjonSaksbehandlerGroupId,
        pensjonVeilederGroupId,
        pensjonBrukerHjelpa,
        pensjonKlageBehandlerGroupId,
        pensjonUfoereGroupId,
        tokenService,
        skjermingClient,
        personService
    )

    //---------------
    //- Basic access
    //---------------
    @Test
    fun `should return nothing when veileder access to innbygger without addressebeskyttelse or skjerming`() {
        val pid = "12345678901"
        `when` (tokenService.getGroups()).thenReturn(listOf(pensjonVeilederGroupId,pensjonUfoereGroupId))
        `when` (skjermingClient.isSkjermet(pid)).thenReturn(false)
        `when` (personService.getAdressebeskyttelsesgrad(pid)).thenReturn(null)
        authorizationService.checkVeilederTilgangTilInnbygger(pid)
    }

    @Test
    fun `should return nothing when veileder access to innbygger with addressebeskyttelse ugradert and no skjerming`() {
        val pid = "12345678901"
        `when` (tokenService.getGroups()).thenReturn(listOf(pensjonVeilederGroupId,pensjonUfoereGroupId))
        `when` (skjermingClient.isSkjermet(pid)).thenReturn(false)
        `when` (personService.getAdressebeskyttelsesgrad(pid)).thenReturn(PdlAdressebeskyttelsesgradering.UGRADERT)
        authorizationService.checkVeilederTilgangTilInnbygger(pid)
    }

    @Test
    fun `should return nothing when saksbehandler access to innbygger with addressebeskyttelse ugradert and no skjerming`() {
        val pid = "12345678901"
        `when` (tokenService.getGroups()).thenReturn(listOf(pensjonSaksbehandlerGroupId,pensjonUfoereGroupId))
        `when` (skjermingClient.isSkjermet(pid)).thenReturn(false)
        `when` (personService.getAdressebeskyttelsesgrad(pid)).thenReturn(PdlAdressebeskyttelsesgradering.UGRADERT)
        authorizationService.checkVeilederTilgangTilInnbygger(pid)
    }

    @Test
    fun `should return nothing when brukerhjelpa access to innbygger with addressebeskyttelse ugradert and no skjerming`() {
        val pid = "12345678901"
        `when` (tokenService.getGroups()).thenReturn(listOf(pensjonBrukerHjelpa,pensjonUfoereGroupId))
        `when` (skjermingClient.isSkjermet(pid)).thenReturn(false)
        `when` (personService.getAdressebeskyttelsesgrad(pid)).thenReturn(PdlAdressebeskyttelsesgradering.UGRADERT)
        authorizationService.checkVeilederTilgangTilInnbygger(pid)
    }

    @Test
    fun `should return nothing when klagebehandler access to innbygger with addressebeskyttelse ugradert and no skjerming`() {
        val pid = "12345678901"
        `when` (tokenService.getGroups()).thenReturn(listOf(pensjonKlageBehandlerGroupId,pensjonUfoereGroupId))
        `when` (skjermingClient.isSkjermet(pid)).thenReturn(false)
        `when` (personService.getAdressebeskyttelsesgrad(pid)).thenReturn(PdlAdressebeskyttelsesgradering.UGRADERT)
        authorizationService.checkVeilederTilgangTilInnbygger(pid)
    }

    @Test
    fun `should return Exception when okonomi access to innbygger with addressebeskyttelse ugradert and no skjerming`() {
        val pid = "12345678901"
        `when` (tokenService.getGroups()).thenReturn(listOf(pensjonOkonomiGroupId,pensjonUfoereGroupId))
        val exception = assertThrows<VeilederUnauthorizedException> { authorizationService.checkVeilederTilgangTilInnbygger(pid) }
        assertEquals(VeilederUnauthorizedExceptionName,exception.toString())
    }

    @Test
    fun `should return Exception when saksbehandler without ufore rolle access to innbygger with addressebeskyttelse ugradert and no skjerming`() {
        val pid = "12345678901"
        `when` (tokenService.getGroups()).thenReturn(listOf(pensjonSaksbehandlerGroupId))
        val exception = assertThrows<VeilederUnauthorizedException> { authorizationService.checkVeilederTilgangTilInnbygger(pid) }
        assertEquals(VeilederUnauthorizedExceptionName,exception.toString())
    }

    //---------------
    //- Skjerming
    //---------------

    @Test
    fun `should return nothing when veileder with access to skjermede access innbygger with skjerming`() {
        val pid = "12345678901"
        `when` (tokenService.getGroups()).thenReturn(listOf(pensjonVeilederGroupId,pensjonUfoereGroupId,skjermetGroupId))
        `when` (skjermingClient.isSkjermet(pid)).thenReturn(true)
        `when` (personService.getAdressebeskyttelsesgrad(pid)).thenReturn(null)
        authorizationService.checkVeilederTilgangTilInnbygger(pid)
    }

    @Test
    fun `should return Exception when veileder without access to skjermede access innbygger with skjerming`() {
        val pid = "12345678901"
        `when` (tokenService.getGroups()).thenReturn(listOf(pensjonVeilederGroupId,pensjonUfoereGroupId))
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
        `when` (tokenService.getGroups()).thenReturn(listOf(pensjonVeilederGroupId,pensjonUfoereGroupId,strengtFortroligAdresseGroupId))
        `when` (skjermingClient.isSkjermet(pid)).thenReturn(false)
        `when` (personService.getAdressebeskyttelsesgrad(pid)).thenReturn(PdlAdressebeskyttelsesgradering.STRENGT_FORTROLIG)
        authorizationService.checkVeilederTilgangTilInnbygger(pid)
    }

    @Test
    fun `should return nothing when veileder with access to strengt fortrolig adresse access innbygger with strengt fortrolig adresse utland`() {
        val pid = "12345678901"
        `when` (tokenService.getGroups()).thenReturn(listOf(pensjonVeilederGroupId,pensjonUfoereGroupId,strengtFortroligAdresseGroupId))
        `when` (skjermingClient.isSkjermet(pid)).thenReturn(false)
        `when` (personService.getAdressebeskyttelsesgrad(pid)).thenReturn(PdlAdressebeskyttelsesgradering.STRENGT_FORTROLIG_UTLAND)
        authorizationService.checkVeilederTilgangTilInnbygger(pid)
    }

    @Test
    fun `should return nothing when veileder with access to fortrolig adresse access innbygger with fortrolig adresse`() {
        val pid = "12345678901"
        `when` (tokenService.getGroups()).thenReturn(listOf(pensjonVeilederGroupId,pensjonUfoereGroupId,fortroligAdresseGroupId))
        `when` (skjermingClient.isSkjermet(pid)).thenReturn(false)
        `when` (personService.getAdressebeskyttelsesgrad(pid)).thenReturn(PdlAdressebeskyttelsesgradering.FORTROLIG)
        authorizationService.checkVeilederTilgangTilInnbygger(pid)
    }

    @Test
    fun `should return nothing when veileder with access to strengt fortrolig adresse access innbygger without adressebeskyttelse - ugradert`() {
        val pid = "12345678901"
        `when` (tokenService.getGroups()).thenReturn(listOf(pensjonVeilederGroupId,pensjonUfoereGroupId,strengtFortroligAdresseGroupId))
        `when` (skjermingClient.isSkjermet(pid)).thenReturn(false)
        `when` (personService.getAdressebeskyttelsesgrad(pid)).thenReturn(PdlAdressebeskyttelsesgradering.UGRADERT)
        authorizationService.checkVeilederTilgangTilInnbygger(pid)
    }

    @Test
    fun `should return Exception when veileder with access to strengt fortrolig adresse access innbygger with fortrolig adresse`() {
        val pid = "12345678901"
        `when` (tokenService.getGroups()).thenReturn(listOf(pensjonVeilederGroupId,pensjonUfoereGroupId,strengtFortroligAdresseGroupId))
        `when` (skjermingClient.isSkjermet(pid)).thenReturn(false)
        `when` (personService.getAdressebeskyttelsesgrad(pid)).thenReturn(PdlAdressebeskyttelsesgradering.FORTROLIG)
        val exception = assertThrows<VeilederUnauthorizedException> { authorizationService.checkVeilederTilgangTilInnbygger(pid) }
        assertEquals(VeilederUnauthorizedExceptionName,exception.toString())
    }

    @Test
    fun `should return Exception when veileder with access to fortrolig adresse access innbygger with strengt fortrolig adresse`() {
        val pid = "12345678901"
        `when` (tokenService.getGroups()).thenReturn(listOf(pensjonVeilederGroupId,pensjonUfoereGroupId,fortroligAdresseGroupId))
        `when` (skjermingClient.isSkjermet(pid)).thenReturn(false)
        `when` (personService.getAdressebeskyttelsesgrad(pid)).thenReturn(PdlAdressebeskyttelsesgradering.STRENGT_FORTROLIG)
        val exception = assertThrows<VeilederUnauthorizedException> { authorizationService.checkVeilederTilgangTilInnbygger(pid) }
        assertEquals(VeilederUnauthorizedExceptionName,exception.toString())
    }

    @Test
    fun `should return Exception when veileder with no access to addressebekyttede access innbygger with strengt fortrolig adresse`() {
        val pid = "12345678901"
        `when` (tokenService.getGroups()).thenReturn(listOf(pensjonVeilederGroupId,pensjonUfoereGroupId))
        `when` (skjermingClient.isSkjermet(pid)).thenReturn(false)
        `when` (personService.getAdressebeskyttelsesgrad(pid)).thenReturn(PdlAdressebeskyttelsesgradering.STRENGT_FORTROLIG)
        val exception = assertThrows<VeilederUnauthorizedException> { authorizationService.checkVeilederTilgangTilInnbygger(pid) }
        assertEquals(VeilederUnauthorizedExceptionName,exception.toString())
    }

    @Test
    fun `should return Exception when veileder with no access to addressebekyttede access innbygger with strengt fortrolig adresse utland`() {
        val pid = "12345678901"
        `when` (tokenService.getGroups()).thenReturn(listOf(pensjonVeilederGroupId,pensjonUfoereGroupId))
        `when` (skjermingClient.isSkjermet(pid)).thenReturn(false)
        `when` (personService.getAdressebeskyttelsesgrad(pid)).thenReturn(PdlAdressebeskyttelsesgradering.STRENGT_FORTROLIG_UTLAND)
        val exception = assertThrows<VeilederUnauthorizedException> { authorizationService.checkVeilederTilgangTilInnbygger(pid) }
        assertEquals(VeilederUnauthorizedExceptionName,exception.toString())
    }

    @Test
    fun `should return Exception when veileder with no access to addressebekyttede access innbygger with fortrolig adresse`() {
        val pid = "12345678901"
        `when` (tokenService.getGroups()).thenReturn(listOf(pensjonVeilederGroupId,pensjonUfoereGroupId))
        `when` (skjermingClient.isSkjermet(pid)).thenReturn(false)
        `when` (personService.getAdressebeskyttelsesgrad(pid)).thenReturn(PdlAdressebeskyttelsesgradering.FORTROLIG)
        val exception = assertThrows<VeilederUnauthorizedException> { authorizationService.checkVeilederTilgangTilInnbygger(pid) }
        assertEquals(VeilederUnauthorizedExceptionName,exception.toString())
    }
}