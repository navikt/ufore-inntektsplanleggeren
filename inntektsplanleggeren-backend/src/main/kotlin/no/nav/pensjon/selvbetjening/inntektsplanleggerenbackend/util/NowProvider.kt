package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.util

import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class NowProvider {
    fun now() = LocalDate.now()
}