package no.nav.ufore.inntektsplanleggeren.security

import no.nav.ufore.inntektsplanleggeren.configuration.AppId
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service

@Service
class TokenService(
    @Value("\${oauth2.azureAd.issuer}") private val azureAdIssuer: String,
    @Value("\${oauth2.tokenX.issuer}") private val tokenXIssuer: String,
    private val azureAdService: AzureAdService,
    private val tokenXService: TokenXService,
) {

    enum class TokenType {
        AZURE_AD_CLIENT_CREDENTIALS, AZURE_AD_ON_BEHALF_OF, TOKEN_X
    }
    enum class Innloggingstype {
        LEVEL4,
        LEVEL3,
        NAV,
        SYSTEM
    }

    fun getEgressToken(scope: String, audience: String? = null, pid: String, appId: AppId): String? =
        SecurityContextHolder.getContext().authentication.let {
            val token = (it as JwtAuthenticationToken).token
            val scopes = listOf(scope)

            when (typeOf(token, pid, appId)) {
                TokenType.AZURE_AD_ON_BEHALF_OF -> azureAdService.exchangeIngressTokenToEgressToken(
                    token.tokenValue,
                    scopes
                )

                TokenType.AZURE_AD_CLIENT_CREDENTIALS -> azureAdService.retrieveClientCredentialsToken(scopes)
                TokenType.TOKEN_X -> audience?.let { audience ->
                    tokenXService.exchangeIngressTokenToEgressToken(token.tokenValue, audience)
                } ?: throw EgressAudienceMissingException()
            }
        }

    fun getInnloggingstype(): Innloggingstype {
        if (isUserLoggedInAsSaksbehandler()) {
            return Innloggingstype.NAV
        }
        if (isUserLoggedInAsPerson()) {
            SecurityContextHolder.getContext().authentication.let {
                val token = (it as JwtAuthenticationToken).token
                val acr = token.getClaim<String>("acr")
                if ("Level4" == acr || "idporten-loa-high" == acr) {
                    return Innloggingstype.LEVEL4
                }
                if ("Level3" == acr || "idporten-loa-substantial" == acr) {
                    return Innloggingstype.LEVEL3
                }
            }
        }
        return Innloggingstype.SYSTEM
    }

    fun isUserLoggedInAsPerson(): Boolean =
        determineTokenType() == TokenType.TOKEN_X

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

    fun determineLoggedInUser(): String {
        SecurityContextHolder.getContext().authentication.let {
            val token = (it as JwtAuthenticationToken).token
            if (determineTokenType() == TokenType.TOKEN_X) {
                return token.getClaim("pid") ?: throw IllegalStateException("Mangler pid claim i token")
            } else if (determineTokenType() == TokenType.AZURE_AD_ON_BEHALF_OF) {
                return token.getClaim("name") ?: throw IllegalStateException("Mangler name claim i token")
            }
        }
        return "SYSTEM"
    }

    fun determineLoggedInUserId(): String {
        SecurityContextHolder.getContext().authentication.let {
            val token = (it as JwtAuthenticationToken).token
            if (determineTokenType() == TokenType.TOKEN_X) {
                return token.getClaim("pid") ?: throw IllegalStateException("Mangler pid claim i token")
            } else if (determineTokenType() == TokenType.AZURE_AD_ON_BEHALF_OF) {
                return token.getClaim("NAVident") ?: throw IllegalStateException("Mangler NAVident claim i token")
            } else if (determineTokenType() == TokenType.AZURE_AD_CLIENT_CREDENTIALS) {
                return token.getClaim("azp_name") ?: throw IllegalStateException("Mangler azp_name claim i token")
            }
        }
        throw RuntimeException("Unknown token type")
    }
    fun isLoginLevelHigh(): Boolean = getInnloggingstype() == Innloggingstype.LEVEL4

    fun determineRequestingPid(): String {
        SecurityContextHolder.getContext().authentication!!.let {
            if (determineTokenType() == TokenType.TOKEN_X) {
                return it.name
            }
            return ""
        }
    }

    fun getGroups(): List<String> {
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