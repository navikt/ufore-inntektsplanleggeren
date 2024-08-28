package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.person.parallellesannheter.dto

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.person.pdl.PdlAdressebskyttelse
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.person.pdl.PdlFoedsel
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.person.pdl.PdlNavn


data class FoedselParallelleSannheterContainer(@JsonProperty("foedsel") val foedsel: List<PdlFoedsel>?)
    : ParallelleSannheterContainer<PdlFoedsel>(foedsel) {
    override fun lockDecision(): FoedselParallelleSannheterContainer {
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