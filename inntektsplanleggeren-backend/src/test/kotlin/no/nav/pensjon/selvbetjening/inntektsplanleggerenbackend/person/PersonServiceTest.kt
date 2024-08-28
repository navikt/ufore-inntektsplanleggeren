package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.person

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.person.parallellesannheter.ParallelleSannheterService
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.person.pdl.*
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security.TokenService
import org.assertj.core.util.Lists
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.mockito.Mockito
import org.mockito.Mockito.*
import java.time.LocalDate

class PersonServiceTest {

    private val pdlClient = Mockito.mock(PdlClient::class.java)
    private val parallelleSannheterService = Mockito.mock(ParallelleSannheterService::class.java)
    private val tokenService = Mockito.mock(TokenService::class.java)

    private val personService = PersonService(pdlClient, parallelleSannheterService, tokenService)

    @Test
    fun getFodselsdato() {
        val date = LocalDate.of(2000, 1, 1)
        `when`(pdlClient.performQuery(any())).thenReturn(PdlPerson(null, null, null))
        `when`(parallelleSannheterService.decideFodselsdato(any())).thenReturn(date)
        val result = personService.getFodselsdato("aaaa")
        assertEquals(date, result)
    }

    @Test
    fun getPersondataForMottaker_saksbehandler_ugradert() {
        val pdlPerson = getPdlPerson(PdlAdressebeskyttelsesgradering.UGRADERT)
        `when`(pdlClient.performQueryWithElevatedPriveleges(any())).thenReturn(pdlPerson)
        `when`(tokenService.isUserLoggedInAsSaksbehandler()).thenReturn(true)
        `when`(tokenService.isUserInFortroligGroup()).thenReturn(false)
        `when`(tokenService.isUserInStrengtFortroligGroup()).thenReturn(false)
        `when`(pdlClient.performQuery(PdlQueryBuilder.getPersondataQuery("aaa"))).thenReturn(pdlPerson)
        `when`(parallelleSannheterService.decideFodselsdato(pdlPerson.foedsel)).thenReturn(pdlPerson.foedsel!!.get(0).foedselsdato)
        `when`(parallelleSannheterService.decideNavn(any())).thenReturn(pdlPerson.navn!!.get(0))
        `when`(parallelleSannheterService.decideAdressebeskyttelse(pdlPerson.adressebeskyttelse)).thenReturn(pdlPerson.adressebeskyttelse!!.get(0))

        val result = personService.getPersondataForMottaker("aaa")

        verify(pdlClient, times(1)).performQuery(PdlQueryBuilder.getPersondataQuery("aaa"))
        assertEquals(LocalDate.of(2000, 1, 1),result!!.fodselsdato)
        assertEquals("Name", result.navn.fornavn)
        assertEquals("MiddleName", result.navn.mellomnavn)
        assertEquals("LastName", result.navn.etternavn)
    }

    @Test
    fun getPersondataForMottaker_saksbehandler_null() {
        val pdlPerson = getPdlPerson(PdlAdressebeskyttelsesgradering.FORTROLIG)
        `when`(pdlClient.performQueryWithElevatedPriveleges(any())).thenReturn(pdlPerson)
        `when`(tokenService.isUserLoggedInAsSaksbehandler()).thenReturn(true)
        `when`(tokenService.isUserInFortroligGroup()).thenReturn(false)
        `when`(tokenService.isUserInStrengtFortroligGroup()).thenReturn(false)
        `when`(pdlClient.performQuery(PdlQueryBuilder.getPersondataQuery("aaa"))).thenReturn(pdlPerson)
        `when`(parallelleSannheterService.decideFodselsdato(pdlPerson.foedsel)).thenReturn(pdlPerson.foedsel!!.get(0).foedselsdato)
        `when`(parallelleSannheterService.decideNavn(any())).thenReturn(pdlPerson.navn!!.get(0))
        `when`(parallelleSannheterService.decideAdressebeskyttelse(pdlPerson.adressebeskyttelse)).thenReturn(null)

        val result = personService.getPersondataForMottaker("aaa")

        verify(pdlClient, times(1)).performQuery(PdlQueryBuilder.getPersondataQuery("aaa"))
        assertEquals(LocalDate.of(2000, 1, 1),result!!.fodselsdato)
        assertEquals("Name", result.navn.fornavn)
        assertEquals("MiddleName", result.navn.mellomnavn)
        assertEquals("LastName", result.navn.etternavn)
    }

    @Test
    fun getPersondataForMottaker_saksbehandler_fortrolig_with_access() {
        val pdlPerson = getPdlPerson(PdlAdressebeskyttelsesgradering.FORTROLIG)
        `when`(pdlClient.performQueryWithElevatedPriveleges(any())).thenReturn(pdlPerson)
        `when`(tokenService.isUserLoggedInAsSaksbehandler()).thenReturn(true)
        `when`(tokenService.isUserInFortroligGroup()).thenReturn(true)
        `when`(tokenService.isUserInStrengtFortroligGroup()).thenReturn(false)
        `when`(pdlClient.performQuery(PdlQueryBuilder.getPersondataQuery("aaa"))).thenReturn(pdlPerson)
        `when`(parallelleSannheterService.decideFodselsdato(pdlPerson.foedsel)).thenReturn(pdlPerson.foedsel!!.get(0).foedselsdato)
        `when`(parallelleSannheterService.decideNavn(any())).thenReturn(pdlPerson.navn!!.get(0))
        `when`(parallelleSannheterService.decideAdressebeskyttelse(pdlPerson.adressebeskyttelse)).thenReturn(pdlPerson.adressebeskyttelse!!.get(0))

        val result = personService.getPersondataForMottaker("aaa")

        verify(pdlClient, times(1)).performQuery(PdlQueryBuilder.getPersondataQuery("aaa"))
        assertEquals(LocalDate.of(2000, 1, 1),result!!.fodselsdato)
        assertEquals("Name", result.navn.fornavn)
        assertEquals("MiddleName", result.navn.mellomnavn)
        assertEquals("LastName", result.navn.etternavn)
    }

    @Test
    fun getPersondataForMottaker_saksbehandler_fortrolig_without_access() {
        val pdlPerson = getPdlPerson(PdlAdressebeskyttelsesgradering.FORTROLIG)
        `when`(pdlClient.performQueryWithElevatedPriveleges(any())).thenReturn(pdlPerson)
        `when`(tokenService.isUserLoggedInAsSaksbehandler()).thenReturn(true)
        `when`(tokenService.isUserInFortroligGroup()).thenReturn(false)
        `when`(tokenService.isUserInStrengtFortroligGroup()).thenReturn(false)
        `when`(parallelleSannheterService.decideFodselsdato(pdlPerson.foedsel)).thenReturn(pdlPerson.foedsel!!.get(0).foedselsdato)
        `when`(parallelleSannheterService.decideNavn(any())).thenReturn(pdlPerson.navn!!.get(0))
        `when`(parallelleSannheterService.decideAdressebeskyttelse(pdlPerson.adressebeskyttelse)).thenReturn(pdlPerson.adressebeskyttelse!!.get(0))

        val result = personService.getPersondataForMottaker("aaa")

        verify(pdlClient, times(0)).performQuery(PdlQueryBuilder.getPersondataQuery("aaa"))
        assertEquals(LocalDate.of(2000, 1, 1),result!!.fodselsdato)
        assertNull(result.navn.fornavn)
        assertNull(result.navn.mellomnavn)
        assertNull(result.navn.etternavn)
    }

    @Test
    fun getPersondataForMottaker_saksbehandler_strengt_fortrolig_with_access() {
        val pdlPerson = getPdlPerson(PdlAdressebeskyttelsesgradering.STRENGT_FORTROLIG)
        `when`(pdlClient.performQueryWithElevatedPriveleges(any())).thenReturn(pdlPerson)
        `when`(tokenService.isUserLoggedInAsSaksbehandler()).thenReturn(true)
        `when`(tokenService.isUserInFortroligGroup()).thenReturn(true)
        `when`(tokenService.isUserInStrengtFortroligGroup()).thenReturn(true)
        `when`(pdlClient.performQuery(PdlQueryBuilder.getPersondataQuery("aaa"))).thenReturn(pdlPerson)
        `when`(parallelleSannheterService.decideFodselsdato(pdlPerson.foedsel)).thenReturn(pdlPerson.foedsel!!.get(0).foedselsdato)
        `when`(parallelleSannheterService.decideNavn(any())).thenReturn(pdlPerson.navn!!.get(0))
        `when`(parallelleSannheterService.decideAdressebeskyttelse(pdlPerson.adressebeskyttelse)).thenReturn(pdlPerson.adressebeskyttelse!!.get(0))

        val result = personService.getPersondataForMottaker("aaa")

        verify(pdlClient, times(1)).performQuery(PdlQueryBuilder.getPersondataQuery("aaa"))
        assertEquals(LocalDate.of(2000, 1, 1),result!!.fodselsdato)
        assertEquals("Name", result.navn.fornavn)
        assertEquals("MiddleName", result.navn.mellomnavn)
        assertEquals("LastName", result.navn.etternavn)
    }

    @Test
    fun getPersondataForMottaker_saksbehandler_strengt_fortrolig_without_access() {
        val pdlPerson = getPdlPerson(PdlAdressebeskyttelsesgradering.STRENGT_FORTROLIG)
        `when`(pdlClient.performQueryWithElevatedPriveleges(any())).thenReturn(pdlPerson)
        `when`(tokenService.isUserLoggedInAsSaksbehandler()).thenReturn(true)
        `when`(tokenService.isUserInFortroligGroup()).thenReturn(true)
        `when`(tokenService.isUserInStrengtFortroligGroup()).thenReturn(false)
        `when`(parallelleSannheterService.decideFodselsdato(pdlPerson.foedsel)).thenReturn(pdlPerson.foedsel!!.get(0).foedselsdato)
        `when`(parallelleSannheterService.decideNavn(any())).thenReturn(pdlPerson.navn!!.get(0))
        `when`(parallelleSannheterService.decideAdressebeskyttelse(pdlPerson.adressebeskyttelse)).thenReturn(pdlPerson.adressebeskyttelse!!.get(0))

        val result = personService.getPersondataForMottaker("aaa")

        verify(pdlClient, times(0)).performQuery(PdlQueryBuilder.getPersondataQuery("aaa"))
        assertEquals(LocalDate.of(2000, 1, 1),result!!.fodselsdato)
        assertNull(result.navn.fornavn)
        assertNull(result.navn.mellomnavn)
        assertNull(result.navn.etternavn)
    }

    @Test
    fun getPersondataForMottaker_annen_bruker_fortrolig() {
        val pdlPerson = getPdlPerson(PdlAdressebeskyttelsesgradering.FORTROLIG)
        `when`(pdlClient.performQueryWithElevatedPriveleges(any())).thenReturn(pdlPerson)
        `when`(tokenService.isUserLoggedInAsSaksbehandler()).thenReturn(false)
        `when`(tokenService.isUserInFortroligGroup()).thenReturn(false)
        `when`(tokenService.isUserInStrengtFortroligGroup()).thenReturn(false)
        `when`(parallelleSannheterService.decideFodselsdato(pdlPerson.foedsel)).thenReturn(pdlPerson.foedsel!!.get(0).foedselsdato)
        `when`(parallelleSannheterService.decideNavn(any())).thenReturn(pdlPerson.navn!!.get(0))
        `when`(parallelleSannheterService.decideAdressebeskyttelse(pdlPerson.adressebeskyttelse)).thenReturn(pdlPerson.adressebeskyttelse!!.get(0))

        val result = personService.getPersondataForMottaker("aaa")

        verify(pdlClient, times(0)).performQuery(PdlQueryBuilder.getPersondataQuery("aaa"))
        assertEquals(LocalDate.of(2000, 1, 1),result!!.fodselsdato)
        assertNull(result.navn.fornavn)
        assertNull(result.navn.mellomnavn)
        assertNull(result.navn.etternavn)
    }

    @Test
    fun getPersondataForMottaker_annen_bruker_isUgradert() {
        val pdlPerson = getPdlPerson(PdlAdressebeskyttelsesgradering.UGRADERT)
        `when`(pdlClient.performQueryWithElevatedPriveleges(any())).thenReturn(pdlPerson)
        `when`(tokenService.isUserLoggedInAsSaksbehandler()).thenReturn(false)
        `when`(tokenService.isUserInFortroligGroup()).thenReturn(false)
        `when`(tokenService.isUserInStrengtFortroligGroup()).thenReturn(false)
        `when`(parallelleSannheterService.decideFodselsdato(pdlPerson.foedsel)).thenReturn(pdlPerson.foedsel!!.get(0).foedselsdato)
        `when`(parallelleSannheterService.decideNavn(any())).thenReturn(pdlPerson.navn!!.get(0))
        `when`(parallelleSannheterService.decideAdressebeskyttelse(pdlPerson.adressebeskyttelse)).thenReturn(pdlPerson.adressebeskyttelse!!.get(0))

        val result = personService.getPersondataForMottaker("aaa")

        verify(pdlClient, times(0)).performQuery(PdlQueryBuilder.getPersondataQuery("aaa"))
        assertEquals(LocalDate.of(2000, 1, 1),result!!.fodselsdato)
        assertEquals("Name", result.navn.fornavn)
        assertEquals("MiddleName", result.navn.mellomnavn)
        assertEquals("LastName", result.navn.etternavn)
    }

    @Test
    fun getAgeAtYear_calculates_age() {
        val resultAge = personService.getAgeAtYear(LocalDate.of(1999, 1, 1), 2015)
        assertEquals(resultAge, 16)
    }

    @Test
    fun getAgeAtYear_throws_exception() {
        val resultAge =
        org.junit.jupiter.api.assertThrows<IllegalStateException> {
            personService.getAgeAtYear(LocalDate.of(2016, 1, 1), 2015) }
    }

    fun getPdlPerson(bekyttelse: PdlAdressebeskyttelsesgradering): PdlPerson {
        val pdlFoedsel = PdlFoedsel(LocalDate.of(2000, 1, 1), null, null, null)
        val pdlNavn = PdlNavn("Name", "MiddleName", "LastName", null, null)
        val pdlAdressebskyttelse = PdlAdressebskyttelse(bekyttelse, null, null)
        return PdlPerson(Lists.list(pdlFoedsel), Lists.list(pdlNavn), Lists.list(pdlAdressebskyttelse))
    }

    private fun <T> any(): T = Mockito.any()
}