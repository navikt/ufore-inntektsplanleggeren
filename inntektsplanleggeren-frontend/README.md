# Inntektsplanleggeren

## Start mock
1. Installer avhentigheter: `npm i`
2. Start: `npm run mock`

## Start mot Q2 eller lokal backend med borger-kontekst
1. Kjør `env-fetch-secrets` (krever nais login). Velg namespace pensjonselvbetjening og deployment inntektsplanleggeren-frontend-borger-q2
2. Lag filen .env.local og legg til variablen `ACCESS_TOKEN` i den. Fila skal ignoreres av git.
    - Du finner tokenet her <https://tokenx-token-generator.intern.dev.nav.no/api/obo?aud=dev-gcp:pensjonselvbetjening:inntektsplanleggeren-backend-q2>
3. Start: `npm run q2` eller `npm run local`
    - For lokal kjøring, kjør opp backend på samme port som ligger i .env.development-local

## Start mot Q2 eller lokal backend med veileder-kontekst
1. Kjør `env-fetch-secrets` (krever nais login). Velg namespace pensjonselvbetjening og deployment inntektsplanleggeren-frontend-veileder-q2
2. Lag filen .env.local og legg til variablen `ACCESS_TOKEN` i den. Fila skal ignoreres av git.
    - Du finner tokenet her <https://azure-token-generator.intern.dev.nav.no/api/obo?aud=dev-gcp:pensjonselvbetjening:inntektsplanleggeren-backend-q2>
3. Start: `npm run q2-veileder` eller `npm run local-veileder`
    - For lokal kjøring, kjør opp backend på samme port som ligger i .env.development-local
4. Legg til `?pid=<brukers_pid>` i url-en