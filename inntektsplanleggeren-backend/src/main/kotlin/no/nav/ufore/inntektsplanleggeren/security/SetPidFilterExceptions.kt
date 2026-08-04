package no.nav.ufore.inntektsplanleggeren.security

class NoFullmaktPresentException : RuntimeException()
class LoginLevelTooLowException : RuntimeException()
class VeilederUnauthorizedException : RuntimeException()