package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security

import ch.qos.logback.access.common.spi.IAccessEvent
import com.fasterxml.jackson.core.JsonGenerator
import net.logstash.logback.composite.AbstractJsonProvider
import org.slf4j.MDC

class MdcProvider : AbstractJsonProvider<IAccessEvent>() {
    override fun writeTo(generator: JsonGenerator, event: IAccessEvent) {
        MDC.getCopyOfContextMap()
            .forEach { (key, value) ->
                generator.writeStringField(key, value)
            }
    }
}