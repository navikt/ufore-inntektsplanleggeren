package no.nav.ufore.inntektsplanleggeren.person.parallellesannheter.dto

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.ufore.inntektsplanleggeren.person.pdl.PdlAdressebskyttelse
import no.nav.ufore.inntektsplanleggeren.person.pdl.PdlFoedselsdato
import no.nav.ufore.inntektsplanleggeren.person.pdl.PdlNavn


data class FoedselsdatoParallelleSannheterContainer(@JsonProperty("foedselsdato") val foedselsdato: List<PdlFoedselsdato>?)
    : ParallelleSannheterContainer<PdlFoedselsdato>(foedselsdato) {
    override fun lockDecision(): FoedselsdatoParallelleSannheterContainer {
        super.lockDecision()
        return this
    }
}

data class NavnParallelleSannheterContainer(@JsonProperty("navn") val navn: List<PdlNavn>?)
    : ParallelleSannheterContainer<PdlNavn>(navn) {
    override fun lockDecision(): NavnParallelleSannheterContainer {
        super.lockDecision()
        return this
    }
}

data class AdressebeskyttelseParallelleSannheterContainer(@JsonProperty("adressebeskyttelse") val adressebeskyttelse: List<PdlAdressebskyttelse>?)
    : ParallelleSannheterContainer<PdlAdressebskyttelse>(adressebeskyttelse) {
    override fun lockDecision(): AdressebeskyttelseParallelleSannheterContainer {
        super.lockDecision()
        return this
    }
}



abstract class ParallelleSannheterContainer<T : ParallellSannhet>(protected val parallelleSannheter: List<T>?) {
    protected var decided = false

    open fun lockDecision(): ParallelleSannheterContainer<T> {
        decided = true
        return this
    }

    fun getSannhet(): List<T> {
        return if (decided) parallelleSannheter?: emptyList() else emptyList()
    }

    open fun isDecisionNecessary(): Boolean {
        if (decided) {
            return false
        }
        return parallelleSannheter != null && parallelleSannheter.filter { it.pdlMetadata?.historisk == false }.size > 1
    }

    fun getHistorical(historisk: Boolean): List<T>? = parallelleSannheter?.filter { it.pdlMetadata?.historisk == historisk }
}