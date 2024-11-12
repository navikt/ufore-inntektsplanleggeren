package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security

class NoFullmaktPresentException : RuntimeException()
class LoginLevelTooLowException : RuntimeException()
class VeilederUnauthorizedException : RuntimeException()