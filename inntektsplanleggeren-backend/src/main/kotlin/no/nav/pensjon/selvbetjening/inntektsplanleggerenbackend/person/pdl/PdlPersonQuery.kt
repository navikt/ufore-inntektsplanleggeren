package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.person.pdl

data class PdlPersonQuery(
        val query: String,
        val variables: PdlPersonVariables
)

data class PdlPersonVariables(
        val ident: String,
        val historisk: Boolean
)