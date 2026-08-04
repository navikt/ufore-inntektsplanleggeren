package no.nav.ufore.inntektsplanleggeren.util

import org.slf4j.MDC

fun getCurrentCallId(): String = MDC.get(NAV_CALL_ID_MDC)