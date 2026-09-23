# Inntektsplanleggeren

## Start mock
1. Installer avhentigheter: `npm i`
2. Start: `npm run mock`

## Start mot Q2 eller lokal backend med borger-kontekst
1. Lag filen .env.local og legg til variablen `ACCESS_TOKEN` i den. Fila skal ignoreres av git.
    - Du finner tokenet her <https://tokenx-token-generator.intern.dev.nav.no/api/obo?aud=dev-gcp:ufore:inntektsplanleggeren-backend-q2>
2. Start: `npm run q2` eller `npm run local`

## Start mot Q2 eller lokal backend med veileder-kontekst
1. Lag filen .env.local og legg til variablen `ACCESS_TOKEN` i den. Fila skal ignoreres av git.
    - Du finner tokenet her <https://azure-token-generator.intern.dev.nav.no/api/obo?aud=dev-gcp:ufore:inntektsplanleggeren-backend-q2>
2. Start: `npm run q2-veileder` eller `npm run local-veileder`
3. Legg til `?pid=<brukers_pid>` i url-en

## Om TOKEN_X_ISSUER / AZURE_OPENID_CONFIG_ISSUER

`npm run local`/`q2` og `npm run local-veileder`/`q2-veileder` setter hhv. `TOKEN_X_ISSUER=dummy` og `AZURE_OPENID_CONFIG_ISSUER=dummy`. Serveren bruker kun *om* variabelen finnes til å velge borger- eller veileder-modus, men selve verdien brukes ikke (`ACCESS_TOKEN` erstatter ekte token-validering lokalt).
