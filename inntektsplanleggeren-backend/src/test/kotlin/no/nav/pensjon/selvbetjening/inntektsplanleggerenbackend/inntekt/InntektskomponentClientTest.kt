package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt


import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.WebClientTest
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.configuration.AppId
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.dto.AbonnerteInntekterIdentOgPeriode
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntekt.dto.Aktoer
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.ClientException
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.ForbiddenException
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security.TokenService
import okhttp3.mockwebserver.MockResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClient
import kotlin.test.assertEquals


class InntektskomponentClientTest: WebClientTest()  {
    val tokenService = Mockito.mock(TokenService::class.java)
    lateinit var inntektskomponentClient: InntektskomponentClient

    @BeforeEach
    override fun setup() {
        super.setup()
        inntektskomponentClient = InntektskomponentClient(
            url = baseUrl, webClient = WebClient.create(), scope = "", audience = "", tokenService = tokenService
        )
    }

    // --- hentAbonnerteInntekterBolk //

    @Test
    fun `should return inntekter when fetching AbonnerteInntekterBolk`() {
        prepare(hentAbonnerteInntekterBolkResponse200HarInntekter())
        val abonnerteInntekterIdentOgPeriode = AbonnerteInntekterIdentOgPeriode(Aktoer("12345678901", "NATURLIG_IDENT"), "fom", "tom")
        val abbonerteInntekterBolk = inntektskomponentClient.hentAbonnerteInntekterBolk(listOf(abonnerteInntekterIdentOgPeriode),"dummFilter","dummyFormaaal")
        abbonerteInntekterBolk.abonnerteInntekterPerIdentListe?.get(0)?.ident?.let {
            assertEquals("12345678901", it.identifikator) }
        abbonerteInntekterBolk.abonnerteInntekterPerIdentListe?.get(0)?.abonnerteInntekterMaanedListe?.get(0)?.sumOpplysningspliktigListe?.get(1)?.let {
            assertEquals(1001.0, it.beloep) }
    }

    @Test
    fun `should return empty list of inntekter when fetching AbonnerteInntekterBolk`() {
        prepare(hentAbonnerteInntekterBolkResponse200HarIngenInntekter())
        val abonnerteInntekterIdentOgPeriode = AbonnerteInntekterIdentOgPeriode(Aktoer("12345678901", "NATURLIG_IDENT"), "fom", "tom")
        val abbonerteInntekterBolk = inntektskomponentClient.hentAbonnerteInntekterBolk(listOf(abonnerteInntekterIdentOgPeriode),"dummFilter","dummyFormaaal")
        assertEquals(emptyList(),abbonerteInntekterBolk.abonnerteInntekterPerIdentListe?.get(0)?.abonnerteInntekterMaanedListe)
    }

    @Test
    fun `should throw ForbiddenException when 403 from inntektskomponenten when fetching AbonnerteInntekterBolk`(){
        prepare(jsonResponse(HttpStatus.FORBIDDEN) ?: MockResponse())
        val exception = assertThrows<ForbiddenException> {
            inntektskomponentClient.hentAbonnerteInntekterBolk(emptyList(),"dummyFilter","dummyFormaaal")
        }
        assertEquals(AppId.INNTEKTSKOMPONENTEN.name, exception.system)
        assertEquals("/api/v1/hentabonnerteinntekterbolk", exception.service)
    }

    @Test
    fun `should throw ClientException when 401 from inntektskomponenten when fetching AbonnerteInntekterBolk`(){
        prepare(jsonResponse(HttpStatus.UNAUTHORIZED) ?: MockResponse())
        val exception = assertThrows<ClientException> {
            inntektskomponentClient.hentAbonnerteInntekterBolk(emptyList(),"dummyFilter","dummyFormaaal")
        }
        assertEquals(AppId.INNTEKTSKOMPONENTEN.name, exception.system)
        assertEquals("/api/v1/hentabonnerteinntekterbolk", exception.service)
    }

    @Test
    fun `should throw ClientException when 400 from inntektskomponenten when fetching AbonnerteInntekterBolk`(){
        val logger: Logger = LoggerFactory.getLogger(InntektskomponentClient::class.java) as Logger
        val listAppender = ListAppender<ILoggingEvent>()
        listAppender.start()
        logger.addAppender(listAppender)
        prepare(hentAbonnerteInntekterBolkResponse400())

        val exception = assertThrows<ClientException> {
            inntektskomponentClient.hentAbonnerteInntekterBolk(emptyList(),"dummyFilter","dummyFormaaal")
        }
        assertEquals(AppId.INNTEKTSKOMPONENTEN.name, exception.system)
        assertEquals("/api/v1/hentabonnerteinntekterbolk", exception.service)
        val logsList = listAppender.list
        assertEquals("Bad request {\"msg\": \"The request is bad\"}", logsList.get(0).getMessage())
    }


    private fun hentAbonnerteInntekterBolkResponse200HarInntekter(): MockResponse {
        return jsonResponse(HttpStatus.OK)!!
            .setBody(
                """
                 {
                     "abonnerteInntekterPerIdentListe": [
                         {
                             "ident": {
                                 "identifikator": "12345678901",
                                 "aktoerType": "NATURLIG_IDENT"
                             },
                             "abonnerteInntekterMaanedListe": [
                                 {
                                     "beloep": "3003.0",
                                     "avviksbeskrivelse": "null",
                                     "maaned": "2024-01",
                                     "sumOpplysningspliktigListe": [
                                         {
                                             "beloep": "2002.0",
                                             "avviksbeskrivelse": "null",
                                             "opplysningspliktig": {
                                                 "identifikator": "123456789",
                                                 "aktoerType": "ORGANISASJON"
                                             }
                                         },
                                         {
                                             "beloep": "1001.0",
                                             "avviksbeskrivelse": "null",
                                             "opplysningspliktig": {
                                                 "identifikator": "223456789",
                                                 "aktoerType": "ORGANISASJON"
                                             }
                                         }
                                     ]
                                 }
                             ]
                         }
                     ],
                     "unntakForIdentListe": []
                   }
                   
            """.trimIndent()
            )
    }
    private fun hentAbonnerteInntekterBolkResponse200HarIngenInntekter(): MockResponse {
        return jsonResponse(HttpStatus.OK)!!
            .setBody(
                """
                 {
                     "abonnerteInntekterPerIdentListe": [
                         {
                             "ident": {
                                 "identifikator": "12345678901",
                                 "aktoerType": "NATURLIG_IDENT"
                             },
                             "abonnerteInntekterMaanedListe": []
                         }
                     ],
                     "unntakForIdentListe": []
                   }
                   
            """.trimIndent()
            )
    }
    private fun hentAbonnerteInntekterBolkResponse400(): MockResponse {
        return jsonResponse(HttpStatus.BAD_REQUEST)!!
            .setBody(
                """
                 {"msg": "The request is bad"}
            """.trimIndent()
            )
    }

    // --- hentForventetInntekt //

    @Test
    fun `should return forventet inntekter when fetching ForventetInntekt`() {
        prepare(hentForventetInntektResponse200())

        val forventetInntektResponse = inntektskomponentClient.hentForventetInntekt("12345678901", listOf(2024,2025))

        assertEquals("2024",forventetInntektResponse.forventetInntektListe?.get(0)?.aar)
        assertEquals(240005,forventetInntektResponse.forventetInntektListe?.get(0)?.beloep)
        assertEquals("2025",forventetInntektResponse.forventetInntektListe?.get(1)?.aar)
        assertEquals(250005,forventetInntektResponse.forventetInntektListe?.get(1)?.beloep)
    }

    @Test
    fun `should return forventet inntekter when fetching ForventetInntekt - ingen inntekt`() {
        prepare(hentForventetInntektResponse200IngenInntekter())

        val forventetInntektResponse = inntektskomponentClient.hentForventetInntekt("12345678901", listOf(2024,2025))

        assertEquals(emptyList(),forventetInntektResponse.forventetInntektListe)
    }

    @Test
    fun `should return forventet inntekter when fetching ForventetInntekt - null inntekt`() {
        prepare(hentForventetInntektResponse200NullInntekter())

        val forventetInntektResponse = inntektskomponentClient.hentForventetInntekt("12345678901", listOf(2024,2025))

        assertEquals(null,forventetInntektResponse.forventetInntektListe)
    }

    @Test
    fun `should throw ForbiddenException when 403 from inntektskomponenten when fetching ForventetInntekt`(){
        prepare(jsonResponse(HttpStatus.FORBIDDEN) ?: MockResponse())
        val exception = assertThrows<ForbiddenException> {
            inntektskomponentClient.hentForventetInntekt("12345678901", listOf(2024))
        }
        assertEquals(AppId.INNTEKTSKOMPONENTEN.name, exception.system)
        assertEquals("/api/v1/forventetinntekt", exception.service)
    }

    @Test
    fun `should throw ClientException when 500 from inntektskomponenten when fetching ForventetInntekt`(){
        prepare(jsonResponse(HttpStatus.INTERNAL_SERVER_ERROR) ?: MockResponse())
        val exception = assertThrows<ClientException> {
            inntektskomponentClient.hentForventetInntekt("12345678901", listOf(2024))
        }
        assertEquals(AppId.INNTEKTSKOMPONENTEN.name, exception.system)
        assertEquals("/api/v1/forventetinntekt", exception.service)
    }

    private fun hentForventetInntektResponse200(): MockResponse {
        return jsonResponse(HttpStatus.OK)!!
            .setBody(
                """
                 {
                     "norskident": "12345678901",
                     "forventetInntektListe": [
                     {
                        "aar": "2024",
                        "beloep": 240005,
                        "type": "type"
                     },
                     {
                        "aar": "2025",
                        "beloep": 250005,
                        "type": "type"
                     }]                     
                 }                  
            """.trimIndent()
            )
    }
    private fun hentForventetInntektResponse200IngenInntekter(): MockResponse {
        return jsonResponse(HttpStatus.OK)!!
            .setBody(
                """
                 {
                     "norskident": "12345678901",
                     "forventetInntektListe": []                     
                 }                  
            """.trimIndent()
            )
    }
    private fun hentForventetInntektResponse200NullInntekter(): MockResponse {
        return jsonResponse(HttpStatus.OK)!!
            .setBody(
                """
                 {
                     "norskident": "12345678901",
                     "forventetInntektListe": null               
                 }                  
            """.trimIndent()
            )
    }
}