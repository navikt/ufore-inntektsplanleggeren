package no.nav.ufore.inntektsplanleggeren.util

import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class NowProvider {
    fun now() = LocalDate.now()
}