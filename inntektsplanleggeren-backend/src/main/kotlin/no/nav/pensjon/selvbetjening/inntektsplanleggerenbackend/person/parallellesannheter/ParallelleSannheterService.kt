package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.person.parallellesannheter

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.person.parallellesannheter.dto.AdressebeskyttelseParallelleSannheterContainer
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.person.parallellesannheter.dto.FoedselsdatoParallelleSannheterContainer
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.person.parallellesannheter.dto.NavnParallelleSannheterContainer
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.person.pdl.PdlAdressebskyttelse
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.person.pdl.PdlFoedselsdato
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.person.pdl.PdlNavn
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class ParallelleSannheterService(private val parallelleSannheterClient: ParallelleSannheterClient) {

    fun decideFodselsdato(foedselsdato: List<PdlFoedselsdato>?): LocalDate? {
        val foedselsdatoParallellSannhetContainer = FoedselsdatoParallelleSannheterContainer(foedselsdato)
        return if (foedselsdatoParallellSannhetContainer.isDecisionNecessary()) {
            parallelleSannheterClient.decideFoedselsdato(foedselsdatoParallellSannhetContainer).getSannhet().firstOrNull()?.foedselsdato
        } else {
            foedselsdatoParallellSannhetContainer.lockDecision().getSannhet().firstOrNull()?.foedselsdato
        }
    }

    fun decideNavn(navn: List<PdlNavn>?): PdlNavn? {
        val navnParallellSannhetContainer = NavnParallelleSannheterContainer(navn)
        return if (navnParallellSannhetContainer.isDecisionNecessary()) {
            parallelleSannheterClient.decideNavn(navnParallellSannhetContainer).getSannhet().firstOrNull()
        } else {
            navnParallellSannhetContainer.lockDecision().getSannhet().firstOrNull()
        }
    }

    fun decideAdressebeskyttelse(adressebskyttelse: List<PdlAdressebskyttelse>?): PdlAdressebskyttelse? {
        val adressebeskyttelseParallellSannhetContainer = AdressebeskyttelseParallelleSannheterContainer(adressebskyttelse)
        return if (adressebeskyttelseParallellSannhetContainer.isDecisionNecessary()) {
            parallelleSannheterClient.decideAdressebeskyttelse(adressebeskyttelseParallellSannhetContainer).getSannhet().firstOrNull()
        } else {
            adressebeskyttelseParallellSannhetContainer.lockDecision().getSannhet().firstOrNull()
        }
    }
}