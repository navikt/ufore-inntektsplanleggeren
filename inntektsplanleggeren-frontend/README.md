# Inntektsplanleggeren

## Start mock

1. Installer avhentigheter: `npm i`
2. Start: `npm run dev`

## Start mot Q2

1. `env-fetch-secrets`
2. Sett miljøvariabler:

- `INNTEKTSPLANLEGGEREN_BACKEND_URL`: <https://inntektsplanleggeren-backend-q2.intern.dev.nav.no>
- `INNTEKTSPLANLEGGEREN_BACKEND_AUDIENCE`: dev-gcp:pensjonselvbetjening:inntektsplanleggeren-backend-q2

3. Hent access token fra <https://tokenx-token-generator.intern.dev.nav.no/api/obo?aud=dev-gcp:pensjonselvbetjening:inntektsplanleggeren-backend-q2>
4. Sett `ACCESS_TOKEN` miljøvariabel
5. Start: `npm run dev-q2`
