package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.person.pdl

class PdlQueryBuilder {
    companion object {
        fun getFoedselQuery(pid: String): PdlPersonQuery {
            return getPdlQuery(pid, "/pdl/foedselsdato.graphql", false)
        }

        fun getAdressebeskyttelseQuery(pid: String): PdlPersonQuery {
            return getPdlQuery(pid, "/pdl/adressebeskyttelse.graphql", false)
        }

        fun getPersonQuery(pid: String): PdlPersonQuery {
            return getPdlQuery(pid, "/pdl/person.graphql", false)
        }

        private fun getPdlQuery(
            pid: String,
            queryFilePath: String,
            historisk: Boolean
        ) = PdlPersonQuery(
            PdlPersonQuery::class.java.getResource(queryFilePath)
                ?.readText()?.replace("[ \n\r]", "")
                ?: throw IllegalArgumentException("Unable to locate graphQl file"),
            PdlPersonVariables(pid, historisk)
        )
    }
}