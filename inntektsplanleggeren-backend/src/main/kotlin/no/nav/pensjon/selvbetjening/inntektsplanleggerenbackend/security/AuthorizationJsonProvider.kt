package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security

import ch.qos.logback.access.common.spi.IAccessEvent
import com.fasterxml.jackson.core.JsonGenerator
import com.nimbusds.jwt.JWTParser
import net.logstash.logback.composite.AbstractJsonProvider
import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.util.Masker

class AuthorizationJsonProvider : AbstractJsonProvider<IAccessEvent>() {

    override fun writeTo(generator: JsonGenerator, event: IAccessEvent) {
        event.getRequestHeader("Authorization")?.let { auth ->
            if (auth.startsWith("Bearer ")) {
                val token = auth.removePrefix("Bearer ").trim()
                try {
                    val jwt = JWTParser.parse(token)
                    jwt.jwtClaimsSet.getStringClaim("NAVident")?.let { navIdent ->
                        generator.writeStringField("nav_ident", navIdent)
                    }
                    jwt.jwtClaimsSet.getStringClaim("pid")?.let { pid ->
                        generator.writeStringField("pid", Masker.maskPid(pid))
                    }
                } catch (e: Exception) {
                    //Trenger ingen videre håndtering
                }
            }
        }
    }
}