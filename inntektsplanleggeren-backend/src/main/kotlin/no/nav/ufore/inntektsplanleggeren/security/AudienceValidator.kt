package no.nav.ufore.inntektsplanleggeren.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component

@Component
class AudienceValidator(@Value("\${oauth2.tokenX.clientId}") val audience: String) : OAuth2TokenValidator<Jwt> {

    override fun validate(jwt: Jwt): OAuth2TokenValidatorResult =
        if (jwt.audience?.contains(audience) == true)
            OAuth2TokenValidatorResult.success()
        else
            OAuth2TokenValidatorResult.failure(OAuth2Error("Invalid audience: ${jwt.audience?.joinToString()}"))
}
