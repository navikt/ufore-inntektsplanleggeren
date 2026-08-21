# Inntektsplanleggeren-backend

## API Documentation
Dev: https://inntektsplanleggeren-backend-q2.intern.dev.nav.no/swagger-ui/index.html

## Lokal utvikling

Du må være lagt til i teamet ufore i Nais Console

For å kjøre backenden lokalt trengs noen miljøvariabler fra Nais. Disse hentes automatisk første gang appen startes med `local`-profilen.
Miljøvariablene må hentes på nytt når appen deployes til testmiljø, det må gjøres manuelt ved å kjøre skriptet `./fetch-secrets.sh --force`.
Skriptet lagrer miljøvariablene i mappen `/tmp` og fjernes når maskinen slåes av.

I IntelliJ kan du velge `edit configurations` ved siden av run-knappen.
* Sett `local` i  active profiles

URL: http://localhost:8080/api/...

### Tokens for test

#### TokenX (Innbygger)
https://tokenx-token-generator.intern.dev.nav.no/api/obo?aud=dev-gcp:ufore:inntektsplanleggeren-backend-q2

#### Azure AD (Veileder)
https://azure-token-generator.intern.dev.nav.no/api/obo?aud=dev-gcp:ufore:inntektsplanleggeren-backend-q2