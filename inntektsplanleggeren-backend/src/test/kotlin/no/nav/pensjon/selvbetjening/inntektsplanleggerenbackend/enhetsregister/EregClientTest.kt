package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.enhetsregister

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.WebClientTest
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.configuration.AppId
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.ClientException
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.ForbiddenException
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.util.NAV_CALL_ID_MDC
import okhttp3.mockwebserver.MockResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.slf4j.MDC
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClient
import java.util.UUID
import kotlin.test.assertEquals

class EregClientTest: WebClientTest()  {
    lateinit var eregClient: EregClient

    @BeforeEach
    override fun setup() {
        super.setup()
        eregClient = EregClient(
            url = baseUrl, webClient = WebClient.create()
        )
        MDC.put(NAV_CALL_ID_MDC, UUID.randomUUID().toString())
    }

    @AfterEach
    override fun tearDown() {
        super.tearDown()
        MDC.remove(NAV_CALL_ID_MDC)
    }

    // --- hentOrganisasjonsnavn ---//

    @Test
    fun `should return name for organisation when successful fetching Organisasjon`() {
        prepare(hentOrgResponse200())
        val organisasjonsnavn = eregClient.hentOrganisasjonsnavn("123456785")
        assertEquals("Min organisasjon", organisasjonsnavn)
    }

    @Test
    fun `should return orgnr as name for organisation when 404 on fetching Organisasjon`() {
        prepare(jsonResponse(HttpStatus.NOT_FOUND) ?: MockResponse())
        val organisasjonsnavn = eregClient.hentOrganisasjonsnavn("123456785")
        assertEquals("123456785", organisasjonsnavn)
    }

    @Test
    fun `should return orgnr as name for organisation when successful fetching Organisasjon without name`() {
        prepare(hentOrgResponse200NoName())
        val organisasjonsnavn = eregClient.hentOrganisasjonsnavn("123456785")
        assertEquals("123456785", organisasjonsnavn)
    }

    @Test
    fun `should throw ForbiddenException when 403 from ereg when fetching Organisasjon`(){
        prepare(jsonResponse(HttpStatus.FORBIDDEN) ?: MockResponse())
        val exception = assertThrows<ForbiddenException> {
            eregClient.hentOrganisasjonsnavn("123456785")
        }
        assertEquals(AppId.EREG.name, exception.system)
        assertEquals("/api/v2/organisasjon/123456785/noekkelinfo", exception.service)
    }

    @Test
    fun `should throw ClientException when 500 from ereg when fetching Organisasjon`(){
        prepare(jsonResponse(HttpStatus.INTERNAL_SERVER_ERROR) ?: MockResponse())
        val exception = assertThrows<ClientException> {
            eregClient.hentOrganisasjonsnavn("123456785")
        }
        assertEquals(AppId.EREG.name, exception.system)
        assertEquals("/api/v2/organisasjon/123456785/noekkelinfo", exception.service)
    }

    private fun hentOrgResponse200(): MockResponse {
        return jsonResponse(HttpStatus.OK)!!
            .setBody(
                """
                 {
                    "organisasjonsnummer": "123456785",
                    "navn": {
                        "sammensattnavn":"Min organisasjon",
                        "navnelinje1": "Min navnelinje 1",
                        "bruksperiode": {"fom": "2018-01-01"},
                        "gyldighetsperiode": {"fom": "2018-01-01"}                        
                    },
                    "enhetstype": "BEDR",
                    "adresse": {
                        "type": "Adressetype",
                        "adresselinje1": "veien 1",
                        "postnummer": "9500",
                        "landkode": "NOR",
                        "kommunenummer": "5403",
                        "bruksperiode": {"fom": "2018-01-01"},
                        "gyldighetsperiode": {"fom": "2018-01-01"}
                    }
                 }                  
            """.trimIndent()
            )
    }

    private fun hentOrgResponse200NoName(): MockResponse {
        return jsonResponse(HttpStatus.OK)!!
            .setBody(
                """
                 {
                    "organisasjonsnummer": "123456785",
                    "navn": null,
                    "enhetstype": "BEDR",
                    "adresse": null
                 }                  
            """.trimIndent()
            )
    }

}