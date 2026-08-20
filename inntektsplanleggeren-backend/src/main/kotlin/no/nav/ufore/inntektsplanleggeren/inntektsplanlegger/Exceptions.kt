package no.nav.ufore.inntektsplanleggeren.inntektsplanlegger

open class PersonNotFoundException(
    override val system: String,
    override val service: String,
    override val message: String?,
    override val cause: Throwable?
) : ClientException(system, service, message, cause)

open class ClientException(
    open val system: String,
    open val service: String,
    override val message: String?,
    override val cause: Throwable?
) : RuntimeException("Feil i service $service i $system. DetailMessage:  $message", cause)

open class ForbiddenException(
    val system: String,
    val service: String,
    override val message: String?,
    override val cause: Throwable?
) : RuntimeException("Tilgang ikke gitt når kaller service $service i $system. DetailMessage:  $message", cause)

class ManglerTilgangInntektskomponentenException(
    val system: String,
    val service: String,
    override val message: String?,
    override val cause: Throwable?
) : RuntimeException("Tilgang ikke gitt når kaller service $service i $system. DetailMessage:  $message", cause)
