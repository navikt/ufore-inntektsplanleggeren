package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.pensjon

import org.springframework.stereotype.Service

@Service
class PensjonService(private val penClient: PenClient) {

    fun getLopendeBeregningsperiode(pid: String): Map<String, Boolean> {
        return penClient.getHasLopendeBeregningsperiode(pid)
    }

}