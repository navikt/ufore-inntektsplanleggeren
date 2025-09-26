package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.configuration

import org.slf4j.MDC

const val NAV_CALL_ID = "Nav-Call-Id"
fun getCurrentCallId(): String? = MDC.get(NAV_CALL_ID)