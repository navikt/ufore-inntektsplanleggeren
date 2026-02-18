package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.util

import org.slf4j.MDC

fun getCurrentCallId(): String = MDC.get(NAV_CALL_ID_MDC)