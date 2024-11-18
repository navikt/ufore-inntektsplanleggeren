# Inntektsplanleggeren-backend
## API Documentation
Dev: https://inntektsplanleggeren-backend-q2.intern.dev.nav.no/swagger-ui/index.html
## How to run locally
Set the following env variables:
* AZURE_APP_CLIENT_SECRET
* TOKEN_X_PRIVATE_JWK

Or use EnvFile - se fetch-secrets.sh

URL: http://localhost:8080/api/..

### Tokens for test
#### TokenX (Innbygger)
https://tokenx-token-generator.intern.dev.nav.no/api/obo?aud=dev-gcp:pensjonselvbetjening:inntektsplanleggeren-backend-q2
#### Azure AD (Veileder)
https://azure-token-generator.intern.dev.nav.no/api/obo?aud=dev-gcp:pensjonselvbetjening:inntektsplanleggeren-backend-q2