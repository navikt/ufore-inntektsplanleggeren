package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.WebClientTest
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.ClientException
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.inntektsplanlegger.ForbiddenException
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon.dto.Inntektsgrunnlag
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security.TokenService
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.util.NAV_CALL_ID_MDC
import okhttp3.mockwebserver.MockResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.slf4j.MDC
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClient
import java.time.*
import java.util.UUID
import kotlin.test.assertEquals

class PenClientTest : WebClientTest() {
    val tokenService = Mockito.mock(TokenService::class.java)
    lateinit var penClient: PenClient

    @BeforeEach
    override fun setup() {
        super.setup()
        penClient = PenClient(
            url = baseUrl, webClient = WebClient.create(), scope = "", audience = "", tokenService = tokenService
        )
        MDC.put(NAV_CALL_ID_MDC, UUID.randomUUID().toString())
    }

    @AfterEach
    override fun tearDown() {
        super.tearDown()
        MDC.remove(NAV_CALL_ID_MDC)
    }

    @Test
    fun `should return InnsendingResponse when 200 from sendInntektsendring `() {
        prepare(innsending200Response())
        val expectedVirkFom = LocalDate.of(2024, 11, 1)
        val expectedFomDato = OffsetDateTime.of(
            LocalDate.of(2024, 11, 1),
            LocalTime.of(0, 0),
            ZoneId.of("Europe/Oslo").rules.getOffset(LocalDate.of(2024, 11, 1).atTime(0, 0, 0))
        )
        val expectedTotalbelopNetto = 4535
        val expectedInntektsgrunnlagBruker = listOf(
            inntektsgrunnlag(belop = 1, fomDato = expectedFomDato),
            inntektsgrunnlag(belop = 2, fomDato = expectedFomDato)
        )
        val expectedInntektsgrunnlagEps = listOf(
            inntektsgrunnlag(belop = 3, fomDato = expectedFomDato),
            inntektsgrunnlag(belop = 4, fomDato = expectedFomDato)
        )

        `when`(tokenService.determineLoggedInUser()).thenReturn("Brukeren")

        val innsendingResponse = penClient.sendInntektsendring(
            PID,
            expectedVirkFom,
            expectedTotalbelopNetto,
            expectedInntektsgrunnlagBruker,
            expectedInntektsgrunnlagEps
        )
        val expectedRequest =
            "{\"pid\":\"00000000001\",\"simulertTotalbelopNetto\":4535,\"virkFom\":\"2024-11-01\",\"forventetInntektBruker\":[{\"inntektsgrunnlagId\":null," +
                    "\"fomDato\":\"2024-11-01T00:00:00+0100\",\"tomDato\":null,\"endringstidspunkt\":null,\"belop\":1,\"bruk\":true,\"kopiertFraGammeltKrav\":false," +
                    "\"registerOpprettetAv\":\"\",\"grunnlagKilde\":\"BRUKER_OPP\",\"registerKilde\":\"SELVBETJ\",\"inntektType\":null,\"inntektHendelseType\":\"BENYTTET\"," +
                    "\"grunnIkkeReduksjonType\":null,\"persongrunnlagId\":1,\"version\":null},{\"inntektsgrunnlagId\":null,\"fomDato\":\"2024-11-01T00:00:00+0100\",\"tomDato\":null," +
                    "\"endringstidspunkt\":null,\"belop\":2,\"bruk\":true,\"kopiertFraGammeltKrav\":false,\"registerOpprettetAv\":\"\",\"grunnlagKilde\":\"BRUKER_OPP\"," +
                    "\"registerKilde\":\"SELVBETJ\",\"inntektType\":null,\"inntektHendelseType\":\"BENYTTET\",\"grunnIkkeReduksjonType\":null,\"persongrunnlagId\":1," +
                    "\"version\":null}],\"forventetInntektEps\":[{\"inntektsgrunnlagId\":null,\"fomDato\":\"2024-11-01T00:00:00+0100\",\"tomDato\":null,\"endringstidspunkt\":null," +
                    "\"belop\":3,\"bruk\":true,\"kopiertFraGammeltKrav\":false,\"registerOpprettetAv\":\"\",\"grunnlagKilde\":\"BRUKER_OPP\",\"registerKilde\":\"SELVBETJ\"," +
                    "\"inntektType\":null,\"inntektHendelseType\":\"BENYTTET\",\"grunnIkkeReduksjonType\":null,\"persongrunnlagId\":1,\"version\":null}," +
                    "{\"inntektsgrunnlagId\":null,\"fomDato\":\"2024-11-01T00:00:00+0100\",\"tomDato\":null,\"endringstidspunkt\":null,\"belop\":4,\"bruk\":true," +
                    "\"kopiertFraGammeltKrav\":false,\"registerOpprettetAv\":\"\",\"grunnlagKilde\":\"BRUKER_OPP\",\"registerKilde\":\"SELVBETJ\",\"inntektType\":null," +
                    "\"inntektHendelseType\":\"BENYTTET\",\"grunnIkkeReduksjonType\":null,\"persongrunnlagId\":1,\"version\":null}],\"innsendtAv\":\"Brukeren\"}"
        val request = takeRequest()

        assertEquals(expectedRequest, request.body.readUtf8())
        assertEquals("/pen/api/selvbetjening/inntektsplanleggeren/behandle", request.path)
        assertEquals("AUTOMATISK_BEHANDLING", innsendingResponse.status)

    }

    @Test
    fun `should throw ForbiddenException when 403 from sendInntektsendring`() {
        prepare(jsonResponse(HttpStatus.FORBIDDEN) ?: MockResponse())
        `when`(tokenService.determineLoggedInUser()).thenReturn("Brukeren")
        val exception = assertThrows<ForbiddenException> {
            penClient.sendInntektsendring(
                PID,
                LocalDate.now(),
                2323,
                emptyList(),
                emptyList()
            )
        }
        assertEquals("PEN", exception.system)
        assertEquals("/pen/api/selvbetjening/inntektsplanleggeren/behandle", exception.service)
    }

    @Test
    fun `should throw ClientException when 500 from sendInntektsendring`() {
        prepare(jsonResponse(HttpStatus.INTERNAL_SERVER_ERROR) ?: MockResponse())
        `when`(tokenService.determineLoggedInUser()).thenReturn("Brukeren")
        val exception = assertThrows<ClientException> {
            penClient.sendInntektsendring(
                PID,
                LocalDate.now(),
                2323,
                emptyList(),
                emptyList()
            )
        }
        assertEquals("PEN", exception.system)
        assertEquals("/pen/api/selvbetjening/inntektsplanleggeren/behandle", exception.service)
    }

    @Test
    fun `should return SimulerEndringUforetrygdResponse when 200 from simulerInntektsendring`() {
        prepare(simulering200Response())
        val expectedVirkFom = LocalDate.of(2024, 11, 1)
        val expectedFomDato = OffsetDateTime.of(
            expectedVirkFom,
            LocalTime.of(0, 0),
            ZoneId.of("Europe/Oslo").rules.getOffset(expectedVirkFom.atTime(0, 0, 0))
        )
        val expectedInntektsgrunnlagBruker = listOf(
            inntektsgrunnlag(belop = 1, fomDato = expectedFomDato),
            inntektsgrunnlag(belop = 2, fomDato = expectedFomDato)
        )
        val expectedInntektsgrunnlagEps = listOf(
            inntektsgrunnlag(belop = 3, fomDato = expectedFomDato),
            inntektsgrunnlag(belop = 4, fomDato = expectedFomDato)
        )

        `when`(tokenService.determineLoggedInUser()).thenReturn("Brukeren")

        val simuleringResponse = penClient.simulerInntektsendring(
            PID,
            expectedVirkFom,
            expectedInntektsgrunnlagBruker,
            expectedInntektsgrunnlagEps
        )

        val request = takeRequest()

        val expectedRequest =
            "{\"pid\":\"00000000001\",\"virk\":\"2024-11-01\",\"inntektsgrunnlagListe\":[{\"inntektsgrunnlagId\":null,\"fomDato\":\"2024-11-01T00:00:00+0100\"," +
                    "\"tomDato\":null,\"endringstidspunkt\":null,\"belop\":1,\"bruk\":true,\"kopiertFraGammeltKrav\":false,\"registerOpprettetAv\":\"\",\"grunnlagKilde\":\"BRUKER_OPP\"," +
                    "\"registerKilde\":\"SELVBETJ\",\"inntektType\":null,\"inntektHendelseType\":\"BENYTTET\",\"grunnIkkeReduksjonType\":null,\"persongrunnlagId\":1,\"version\":null}," +
                    "{\"inntektsgrunnlagId\":null,\"fomDato\":\"2024-11-01T00:00:00+0100\",\"tomDato\":null,\"endringstidspunkt\":null,\"belop\":2,\"bruk\":true,\"kopiertFraGammeltKrav\":false," +
                    "\"registerOpprettetAv\":\"\",\"grunnlagKilde\":\"BRUKER_OPP\",\"registerKilde\":\"SELVBETJ\",\"inntektType\":null,\"inntektHendelseType\":\"BENYTTET\"," +
                    "\"grunnIkkeReduksjonType\":null,\"persongrunnlagId\":1,\"version\":null}],\"inntektsgrunnlagListeEps\":[{\"inntektsgrunnlagId\":null,\"fomDato\":\"2024-11-01T00:00:00+0100\"," +
                    "\"tomDato\":null,\"endringstidspunkt\":null,\"belop\":3,\"bruk\":true,\"kopiertFraGammeltKrav\":false,\"registerOpprettetAv\":\"\",\"grunnlagKilde\":\"BRUKER_OPP\"," +
                    "\"registerKilde\":\"SELVBETJ\",\"inntektType\":null,\"inntektHendelseType\":\"BENYTTET\",\"grunnIkkeReduksjonType\":null,\"persongrunnlagId\":1,\"version\":null}," +
                    "{\"inntektsgrunnlagId\":null,\"fomDato\":\"2024-11-01T00:00:00+0100\",\"tomDato\":null,\"endringstidspunkt\":null,\"belop\":4,\"bruk\":true,\"kopiertFraGammeltKrav\":false," +
                    "\"registerOpprettetAv\":\"\",\"grunnlagKilde\":\"BRUKER_OPP\",\"registerKilde\":\"SELVBETJ\",\"inntektType\":null,\"inntektHendelseType\":\"BENYTTET\",\"grunnIkkeReduksjonType\":null," +
                    "\"persongrunnlagId\":1,\"version\":null}]}"

        assertEquals(expectedRequest, request.body.readUtf8())
        assertEquals("/pen/api/selvbetjening/inntektsplanleggeren/simuler", request.path)

        assertEquals(
            197199,
            simuleringResponse.currentUforetrygdSummary.uforetrygdYtelseskomponenter.uforetrygdOrdiner.amountPerYear
        )
        assertEquals(
            16850,
            simuleringResponse.currentUforetrygdSummary.uforetrygdYtelseskomponenter.uforetrygdOrdiner.amountPerMonth
        )
        assertEquals(
            48890,
            simuleringResponse.currentUforetrygdSummary.uforetrygdYtelseskomponenter.barnetilleggFellesbarn?.amountPerYear
        )
        assertEquals(
            4134,
            simuleringResponse.currentUforetrygdSummary.uforetrygdYtelseskomponenter.barnetilleggFellesbarn?.amountPerMonth
        )
        assertEquals(
            1,
            simuleringResponse.currentUforetrygdSummary.uforetrygdYtelseskomponenter.barnetilleggSaerkullsbarn?.amountPerYear
        )
        assertEquals(
            2,
            simuleringResponse.currentUforetrygdSummary.uforetrygdYtelseskomponenter.barnetilleggSaerkullsbarn?.amountPerMonth
        )
        assertEquals(
            55,
            simuleringResponse.currentUforetrygdSummary.uforetrygdYtelseskomponenter.gjenlevendetillegg?.amountPerYear
        )
        assertEquals(
            66,
            simuleringResponse.currentUforetrygdSummary.uforetrygdYtelseskomponenter.gjenlevendetillegg?.amountPerMonth
        )
        assertEquals(
            3,
            simuleringResponse.currentUforetrygdSummary.uforetrygdYtelseskomponenter.ektefelletillegg?.amountPerYear
        )
        assertEquals(
            4,
            simuleringResponse.currentUforetrygdSummary.uforetrygdYtelseskomponenter.ektefelletillegg?.amountPerMonth
        )
        assertEquals(20984, simuleringResponse.currentUforetrygdSummary.totalbelopNetto)
        assertEquals(246089, simuleringResponse.currentUforetrygdSummary.sumYtelseskomponenter)
        assertEquals(251814.0, simuleringResponse.currentUforetrygdSummary.totalbelopNettoAr)

        assertEquals(
            76,
            simuleringResponse.simulertUforetrygdSummary.uforetrygdYtelseskomponenter.uforetrygdOrdiner.amountPerYear
        )
        assertEquals(
            56,
            simuleringResponse.simulertUforetrygdSummary.uforetrygdYtelseskomponenter.uforetrygdOrdiner.amountPerMonth
        )
        assertEquals(
            45,
            simuleringResponse.simulertUforetrygdSummary.uforetrygdYtelseskomponenter.barnetilleggFellesbarn?.amountPerYear
        )
        assertEquals(
            34,
            simuleringResponse.simulertUforetrygdSummary.uforetrygdYtelseskomponenter.barnetilleggFellesbarn?.amountPerMonth
        )
        assertEquals(
            134,
            simuleringResponse.simulertUforetrygdSummary.uforetrygdYtelseskomponenter.barnetilleggSaerkullsbarn?.amountPerYear
        )
        assertEquals(
            43,
            simuleringResponse.simulertUforetrygdSummary.uforetrygdYtelseskomponenter.barnetilleggSaerkullsbarn?.amountPerMonth
        )
        assertEquals(
            8767,
            simuleringResponse.simulertUforetrygdSummary.uforetrygdYtelseskomponenter.gjenlevendetillegg?.amountPerYear
        )
        assertEquals(
            4932,
            simuleringResponse.simulertUforetrygdSummary.uforetrygdYtelseskomponenter.gjenlevendetillegg?.amountPerMonth
        )
        assertEquals(
            332,
            simuleringResponse.simulertUforetrygdSummary.uforetrygdYtelseskomponenter.ektefelletillegg?.amountPerYear
        )
        assertEquals(
            43,
            simuleringResponse.simulertUforetrygdSummary.uforetrygdYtelseskomponenter.ektefelletillegg?.amountPerMonth
        )
        assertEquals(56757, simuleringResponse.simulertUforetrygdSummary.totalbelopNetto)
        assertEquals(345345, simuleringResponse.simulertUforetrygdSummary.sumYtelseskomponenter)
        assertEquals(353445.0, simuleringResponse.simulertUforetrygdSummary.totalbelopNettoAr)

        assertFalse(simuleringResponse.containsMotregning)
        assertEquals(183136, simuleringResponse.sumHittilUtbetaltIAr)
        assertEquals(-153414, simuleringResponse.sumNettoRestArWithoutBTandET)
        assertEquals(202203.0, simuleringResponse.sumBruttoRestArWithoutBTandET)
        assertEquals(460060, simuleringResponse.inntektstak)
        assertEquals(LocalDate.of(2024, 7, 1), simuleringResponse.gjeldendeBeregningFom)
        assertFalse(simuleringResponse.isFaktoromregnetEllerManueltOverstyrt)
        assertTrue(simuleringResponse.hasOpenInntektsendringskrav)
        assertEquals(LocalDate.of(2023, 10, 1), simuleringResponse.firstVedtakFom)
        assertNull(simuleringResponse.lastVedtakTom)
    }

    @Test
    fun `should throw ForbiddenException when 403 from simulerInntektsendring`() {
        prepare(jsonResponse(HttpStatus.FORBIDDEN) ?: MockResponse())
        `when`(tokenService.determineLoggedInUser()).thenReturn("Brukeren")
        val exception = assertThrows<ForbiddenException> {
            penClient.simulerInntektsendring(
                PID,
                LocalDate.now(),
                emptyList(),
                emptyList()
            )
        }
        assertEquals("PEN", exception.system)
        assertEquals("/pen/api/selvbetjening/inntektsplanleggeren/simuler", exception.service)
    }

    @Test
    fun `should throw ClientException when 500 from simulerInntektsendring`() {
        prepare(jsonResponse(HttpStatus.INTERNAL_SERVER_ERROR) ?: MockResponse())
        `when`(tokenService.determineLoggedInUser()).thenReturn("Brukeren")
        val exception = assertThrows<ClientException> {
            penClient.simulerInntektsendring(
                PID,
                LocalDate.now(),
                emptyList(),
                emptyList()
            )
        }
        assertEquals("PEN", exception.system)
        assertEquals("/pen/api/selvbetjening/inntektsplanleggeren/simuler", exception.service)
    }

    @Test
    fun `should return uforetrygd when 200 from fetchInntektsplanleggerData`() {
        prepare(uforetrygd200Response())
        val expectedEndringstidspunkt = OffsetDateTime.parse("2024-09-11T22:00Z")

        `when`(tokenService.determineLoggedInUser()).thenReturn("Brukeren")

        val uforetrygd = penClient.fetchInntektsplanleggerData(
            PID,
            LocalDate.of(2024, 11, 1)
        )

        val request = takeRequest()

        assertEquals("/pen/api/selvbetjening/inntektsplanleggeren/data?simuleringFom=2024-11-01", request.path)

        assertEquals("19447917729", uforetrygd?.epsPid)
        assertEquals(49611, uforetrygd?.inntektsgrense)
        assertEquals(460060, uforetrygd?.grenseStoppAvUfoeretrygd)
        assertTrue(uforetrygd!!.hasLopendeUforeVedtakThisYear)
        assertFalse(uforetrygd.hasLopendeUforeVedtakNextYear)
        assertFalse(uforetrygd.hasVarigTilrettelagtArbeid)
        assertFalse(uforetrygd.hasGjenlevendeTillegg)
        assertTrue(uforetrygd.uforeHeleAaret)
        assertFalse(uforetrygd.barnetilleggSaerkullsbarn)
        assertTrue(uforetrygd.barnetilleggFellesbarn)
        assertEquals(2, uforetrygd.inntekterFromOpenKravBruker!!.size)
        assertEquals(75000, uforetrygd.inntekterFromOpenKravBruker[0].belop)
        assertEquals("FORINTARB", uforetrygd.inntekterFromOpenKravBruker[0].inntektType)
        assertTrue(uforetrygd.inntekterFromOpenKravBruker[0].bruk)
        assertEquals(expectedEndringstidspunkt, uforetrygd.inntekterFromOpenKravBruker[0].endringstidspunkt)
        assertEquals(2, uforetrygd.inntekterFromOpenKravEps!!.size)
        assertEquals(0, uforetrygd.inntekterFromOpenKravEps[0].belop)
        assertEquals("FORINTARB", uforetrygd.inntekterFromOpenKravBruker[0].inntektType)
        assertTrue(uforetrygd.inntekterFromOpenKravBruker[0].bruk)
    }

    @Test
    fun `should return null when 404 from fetchInntektsplanleggerData`() {
        prepare(jsonResponse(HttpStatus.NOT_FOUND) ?: MockResponse())
        `when`(tokenService.determineLoggedInUser()).thenReturn("Brukeren")
        assertNull(penClient.fetchInntektsplanleggerData(PID, LocalDate.now()))
    }

    @Test
    fun `should throw ForbiddenException when 403 from fetchInntektsplanleggerData`() {
        prepare(jsonResponse(HttpStatus.FORBIDDEN) ?: MockResponse())
        `when`(tokenService.determineLoggedInUser()).thenReturn("Brukeren")
        val exception = assertThrows<ForbiddenException> {
            penClient.fetchInntektsplanleggerData(
                PID,
                LocalDate.now()
            )
        }
        assertEquals("PEN", exception.system)
        assertEquals("/pen/api/selvbetjening/inntektsplanleggeren/data", exception.service)
    }

    @Test
    fun `should throw ClientException when 500 from fetchInntektsplanleggerData`() {
        prepare(jsonResponse(HttpStatus.INTERNAL_SERVER_ERROR) ?: MockResponse())
        `when`(tokenService.determineLoggedInUser()).thenReturn("Brukeren")
        val exception = assertThrows<ClientException> {
            penClient.fetchInntektsplanleggerData(
                PID,
                LocalDate.now()
            )
        }
        assertEquals("PEN", exception.system)
        assertEquals("/pen/api/selvbetjening/inntektsplanleggeren/data", exception.service)
    }

    private fun innsending200Response(): MockResponse {
        return jsonResponse(HttpStatus.OK)!!
            .setBody(
                """
                 {
                    "status": "AUTOMATISK_BEHANDLING"
                  }
            """.trimIndent()
            )
    }

    private fun simulering200Response(): MockResponse {
        return jsonResponse(HttpStatus.OK)!!
            .setBody(
                """
{
    "currentUforetrygdSummary": {
        "uforetrygdYtelseskomponenter": {
            "uforetrygdOrdiner": {
                "amountPerYear": 197199,
                "amountPerMonth": 16850
            },
            "barnetilleggFellesbarn": {
                "amountPerYear": 48890,
                "amountPerMonth": 4134
            },
            "barnetilleggSaerkullsbarn": {
                "amountPerYear": 1,
                "amountPerMonth": 2
            },
            "ektefelletillegg": {
                "amountPerYear": 3,
                "amountPerMonth": 4
            },
            "gjenlevendetillegg": {
                "amountPerYear": 55,
                "amountPerMonth": 66
            }
        },
        "totalbelopNettoAr": 251814.0,
        "totalbelopNetto": 20984,
        "sumYtelseskomponenter": 246089
    },
    "simulertUforetrygdSummary": {
        "uforetrygdYtelseskomponenter": {
            "uforetrygdOrdiner": {
                "amountPerYear": 76,
                "amountPerMonth": 56
            },
            "barnetilleggFellesbarn": {
                "amountPerYear": 45,
                "amountPerMonth": 34
            },
            "barnetilleggSaerkullsbarn": {
                "amountPerYear": 134,
                "amountPerMonth": 43
            },
            "ektefelletillegg": {
                "amountPerYear": 332,
                "amountPerMonth": 43
            },
            "gjenlevendetillegg": {
                "amountPerYear": 8767,
                "amountPerMonth": 4932
            }
        },
        "totalbelopNettoAr": 353445.0,
        "totalbelopNetto": 56757,
        "sumYtelseskomponenter": 345345
    },
    "containsMotregning": false,
    "sumHittilUtbetaltIAr": 183136,
    "sumNettoRestArWithoutBTandET": -153414,
    "sumBruttoRestArWithoutBTandET": 202203.0,
    "inntektstak": 460060,
    "gjeldendeBeregningFom": "2024-07-01",
    "isFaktoromregnetEllerManueltOverstyrt": false,
    "hasOpenInntektsendringskrav": true,
    "firstVedtakFom": "2023-10-01",
    "lastVedtakTom": null,
    "forventedInntektBefore": null,
    "harOpphorteYtelseskomponenter": false
}
                """.trimIndent()
            )
    }

    private fun uforetrygd200Response(): MockResponse {
        return jsonResponse(HttpStatus.OK)!!
            .setBody(
                """
                    {
    "inntektsgrense": 49611,
    "kompensasjonsgrad": 49.7,
    "grenseStoppAvUfoeretrygd": 460060,
    "hasLopendeUforeVedtakThisYear": true,
    "hasLopendeUforeVedtakNextYear": false,
    "hasVarigTilrettelagtArbeid": false,
    "hasGjenlevendeTillegg": false,
    "uforeHeleAaret": true,
    "barnetilleggSaerkullsbarn": false,
    "grenseStoppAvBarnetilleggSaerkullsbarn": null,
    "fribelopBarnetilleggSaerkullsbarn": null,
    "barnetilleggFellesbarn": true,
    "grenseStoppAvBarnetilleggFellesbarn": 669751,
    "fribelopBarnetilleggFellesbarn": 570529,
    "epsPid": "19447917729",
    "inntekterFromOpenKravBruker": [
        {
            "inntektsgrunnlagId": 206812344,
            "fomDato": "2024-10-01T00:00:00+0200",
            "tomDato": "2024-12-31T00:00:00+0100",
            "endringstidspunkt": "2024-09-12T00:00:00+0200",
            "belop": 75000,
            "bruk": true,
            "kopiertFraGammeltKrav": false,
            "registerOpprettetAv": null,
            "grunnlagKilde": "BRUKER_OPP",
            "registerKilde": "SELVBETJ",
            "inntektType": "FORINTARB",
            "inntektHendelseType": "BENYTTET",
            "grunnIkkeReduksjonType": null,
            "persongrunnlagId": 44579124,
            "changeStamp": {
                "createdDate": "2024-09-12T14:06:20+0200",
                "createdBy": "Z990231",
                "updatedDate": "2024-09-12T14:06:20+0200",
                "updatedBy": "Z990231"
            },
            "version": 0,
            "forventetInntekt": true
        },
        {
            "inntektsgrunnlagId": 206812345,
            "fomDato": "2024-10-01T00:00:00+0200",
            "tomDato": "2024-12-31T00:00:00+0100",
            "endringstidspunkt": "2024-09-12T00:00:00+0200",
            "belop": 0,
            "bruk": true,
            "kopiertFraGammeltKrav": false,
            "registerOpprettetAv": null,
            "grunnlagKilde": "BRUKER_OPP",
            "registerKilde": "SELVBETJ",
            "inntektType": "FORINTNAE",
            "inntektHendelseType": "BENYTTET",
            "grunnIkkeReduksjonType": null,
            "persongrunnlagId": 44579124,
            "changeStamp": {
                "createdDate": "2024-09-12T14:06:20+0200",
                "createdBy": "Z990231",
                "updatedDate": "2024-09-12T14:06:20+0200",
                "updatedBy": "Z990231"
            },
            "version": 0,
            "forventetInntekt": true
        }
    ],
    "inntekterFromOpenKravEps": [
        {
            "inntektsgrunnlagId": 206812349,
            "fomDato": "2024-10-01T00:00:00+0200",
            "tomDato": "2024-12-31T00:00:00+0100",
            "endringstidspunkt": "2024-09-12T00:00:00+0200",
            "belop": 0,
            "bruk": true,
            "kopiertFraGammeltKrav": false,
            "registerOpprettetAv": null,
            "grunnlagKilde": "BRUKER_OPP",
            "registerKilde": "SELVBETJ",
            "inntektType": "FORINTARB",
            "inntektHendelseType": "BENYTTET",
            "grunnIkkeReduksjonType": null,
            "persongrunnlagId": 44579125,
            "changeStamp": {
                "createdDate": "2024-09-12T14:06:20+0200",
                "createdBy": "Z990231",
                "updatedDate": "2024-09-12T14:06:20+0200",
                "updatedBy": "Z990231"
            },
            "version": 0,
            "forventetInntekt": true
        },
        {
            "inntektsgrunnlagId": 206812350,
            "fomDato": "2024-10-01T00:00:00+0200",
            "tomDato": "2024-12-31T00:00:00+0100",
            "endringstidspunkt": "2024-09-12T00:00:00+0200",
            "belop": 0,
            "bruk": true,
            "kopiertFraGammeltKrav": false,
            "registerOpprettetAv": null,
            "grunnlagKilde": "BRUKER_OPP",
            "registerKilde": "SELVBETJ",
            "inntektType": "FORINTNAE",
            "inntektHendelseType": "BENYTTET",
            "grunnIkkeReduksjonType": null,
            "persongrunnlagId": 44579125,
            "changeStamp": {
                "createdDate": "2024-09-12T14:06:20+0200",
                "createdBy": "Z990231",
                "updatedDate": "2024-09-12T14:06:20+0200",
                "updatedBy": "Z990231"
            },
            "version": 0,
            "forventetInntekt": true
        }
    ]
}
                    """
            )
    }

    private fun inntektsgrunnlag(
        inntektsgrunnlagId: Long? = null,
        fomDato: OffsetDateTime = OffsetDateTime.now(),
        tomDato: OffsetDateTime? = null,
        endringstidspunkt: OffsetDateTime? = null,
        belop: Int = 0,
        bruk: Boolean = true,
        kopiertFraGammeltKrav: Boolean = false,
        registerOpprettetAv: String = "",
        grunnlagKilde: String = "BRUKER_OPP",
        registerKilde: String = "SELVBETJ",
        inntektType: String? = null,
        inntektHendelseType: String = "BENYTTET",
        grunnIkkeReduksjonType: String? = null,
        persongrunnlagId: Long = 1L,
        changeStamp: String? = null,
        version: Int? = null
    ) =
        Inntektsgrunnlag(
            inntektsgrunnlagId = inntektsgrunnlagId,
            fomDato = fomDato,
            tomDato = tomDato,
            endringstidspunkt = endringstidspunkt,
            belop = belop,
            bruk = bruk,
            kopiertFraGammeltKrav = kopiertFraGammeltKrav,
            registerOpprettetAv = registerOpprettetAv,
            grunnlagKilde = grunnlagKilde,
            registerKilde = registerKilde,
            inntektType = inntektType,
            inntektHendelseType = inntektHendelseType,
            grunnIkkeReduksjonType = grunnIkkeReduksjonType,
            persongrunnlagId = persongrunnlagId,
            changeStamp = changeStamp,
            version = version
        )

    companion object {
        private const val PID = "00000000001"
    }

}