package no.nav.ufore.inntektsplanleggeren.enhetsregister

import org.springframework.stereotype.Service

@Service
class EregService(private val eregClient: EregClient) {
    fun getOrganisasjonsnavn(organisasjonsnummer: String): String {
        return eregClient.hentOrganisasjonsnavn(organisasjonsnummer)
    }
}