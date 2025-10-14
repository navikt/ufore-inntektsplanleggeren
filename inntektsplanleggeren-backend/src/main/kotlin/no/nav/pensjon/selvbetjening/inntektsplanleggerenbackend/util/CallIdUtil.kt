package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.util

import org.slf4j.MDC

const val NAV_CALL_ID = "Nav-Call-Id"
const val NAV_CALL_ID_MDC = "nav_call_id"
fun getCurrentCallId(): String? = MDC.get(NAV_CALL_ID)