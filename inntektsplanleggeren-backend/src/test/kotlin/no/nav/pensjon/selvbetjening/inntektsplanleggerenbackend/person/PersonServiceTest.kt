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