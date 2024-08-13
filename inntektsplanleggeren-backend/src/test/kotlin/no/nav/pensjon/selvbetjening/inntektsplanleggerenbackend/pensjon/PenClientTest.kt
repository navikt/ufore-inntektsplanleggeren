package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.WebClientTest
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security.TokenService
import okhttp3.mockwebserver.MockResponse
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDate

class PenClientTest : WebClientTest() {
    val tokenService = Mockito.mock(TokenService::class.java)
    lateinit var penClient: PenClient

    @BeforeEach
    override fun setup() {
        super.setup()
        penClient = PenClient(
            url = baseUrl, webClient = WebClient.create(), scope = "", audience = "", tokenService = tokenService
        )
    }

}