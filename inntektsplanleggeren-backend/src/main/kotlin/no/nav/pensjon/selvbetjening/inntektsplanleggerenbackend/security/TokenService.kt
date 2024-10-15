package no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.security

import no.nav.pensjon.selvbetjening.inntektsplanleggerenbackend.configuration.AppId
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service

@Service
class TokenService(
    @Value("\${oauth2.azureAd.issuer}") private val azureAdIssuer: String,
    @Value("\${oauth2.tokenX.issuer}") private val tokenXIssuer: String,
    @Value("\${strengt-fortrolig-tilgang.group.id}") private val strengtFortroligAdresseGroupId: String,
    @Value("\${fortrolig-tilgang.group.id}") private val fortroligAdresseGroupId: String,
    @Value("\${skjermet-tilgang.group.id}") private val skjermetGroupId: String,
    private val azureAdService: AzureAdService,
    private val tokenXService: TokenXService,
) {

    enum class TokenType {
        AZURE_AD_CLIENT_CREDENTIALS, AZURE_AD_ON_BEHALF_OF, TOKEN_X
    }

    fun getEgressToken(scope: String, audience: String? = null, pid: String, appId: AppId): String? =
        SecurityContextHolder.getContext().authentication.let {
            val token = (it as JwtAuthenticationToken).token
            val scopes = listOf(scope)

            when (typeOf(token, pid, appId)) {
                TokenType.AZURE_AD_ON_BEHALF_OF -> azureAdService.exchangeIngressTokenToEgressToken(token.tokenValue, scopes)
                TokenType.AZURE_AD_CLIENT_CREDENTIALS -> azureAdService.retrieveClientCredentialsToken(scopes)
                TokenType.TOKEN_X -> audience?.let { audience ->
                    tokenXService.exchangeIngressTokenToEgressToken(token.tokenValue, audience)
                } ?: throw EgressAudienceMissingException()
            }
        }

    fun isUserLoggedInAsSaksbehandler(): Boolean =
        determineTokenType() == TokenType.AZURE_AD_ON_BEHALF_OF

    fun determineTokenType(): TokenType {
        SecurityContextHolder.getContext().authentication.let {
            val token = (it as JwtAuthenticationToken).token
            val issuer = token.getClaim<String>("iss")
            if (issuer == azureAdIssuer) {
                if (token.getClaim<String>("roles") == null) {
                    return TokenType.AZURE_AD_ON_BEHALF_OF
                } else if (token.getClaim<String>("sub") == token.getClaim<String>("oid")) {
                    return TokenType.AZURE_AD_CLIENT_CREDENTIALS
                }
            } else if (issuer == tokenXIssuer) {
                return TokenType.TOKEN_X
            }
            throw IllegalStateException("Unknown token type")
        }
    }

    fun determineRequestingPid(): String {
        SecurityContextHolder.getContext().authentication.let {
            if (determineTokenType() == TokenType.TOKEN_X) {
                return it.name
            }
            return ""
        }
    }

    fun determineLoggedInUser(): String {
        SecurityContextHolder.getContext().authentication.let {
            val token = (it as JwtAuthenticationToken).token
            if (determineTokenType() == TokenType.TOKEN_X) {
                return token.getClaim("pid")
            } else if (determineTokenType() == TokenType.AZURE_AD_ON_BEHALF_OF) {
                return token.getClaim("name")
            }
        }
        return "SYSTEM"
    }

    fun isUserInStrengtFortroligGroup(): Boolean = getGroups().contains(strengtFortroligAdresseGroupId)

    fun isUserInFortroligGroup(): Boolean = getGroups().contains(fortroligAdresseGroupId)

    fun isUserInSkjermetGroup(): Boolean = getGroups().contains(skjermetGroupId)

    private fun getGroups(): List<String> {
        SecurityContextHolder.getContext().authentication.let {
            val token = (it as JwtAuthenticationToken).token
            val groups = token.claims["groups"] as List<*>
            return groups.map { groupObject -> groupObject.toString() }
        }
    }

    private fun typeOf(jwt: Jwt, pid: String, appId: AppId): TokenType {
        val issuer = jwt.getClaim<String>("iss")
        if (issuer == azureAdIssuer) {
            if (jwt.getClaim<String>("roles") == null) {
                return TokenType.AZURE_AD_ON_BEHALF_OF
            } else if (jwt.getClaim<String>("sub") == jwt.getClaim<String>("oid")) {
                return TokenType.AZURE_AD_CLIENT_CREDENTIALS
            }
        } else if (issuer == tokenXIssuer) {

            if (appId.supportsTokenX) {

                val pidFromToken = jwt.getClaim<String>("pid")
                val isFullmaktToken = pid != pidFromToken

                if (isFullmaktToken) {
                    return if (appId.supportsFullmakt) TokenType.TOKEN_X else TokenType.AZURE_AD_CLIENT_CREDENTIALS
                }
                return TokenType.TOKEN_X
            }

            return TokenType.AZURE_AD_CLIENT_CREDENTIALS
        }
        throw CouldNotDetermineTokenTypeException()
    }
}

class EgressAudienceMissingException : RuntimeException("Audience missing when scoping token for outgoing call")
class CouldNotDetermineTokenTypeException : RuntimeException("Unable to determine type of token")