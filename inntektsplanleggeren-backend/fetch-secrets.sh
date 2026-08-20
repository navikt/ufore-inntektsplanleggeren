#!/usr/bin/env bash

# Henter TokenX- og AzureAd-secrets for inntektsplanleggeren-backend og lagrer de under /tmp/ - her slettes alt ved restart av maskinen

envFile="/tmp/inntektsplanleggeren.env"
reason="Kjøre inntektsplanleggeren-backend lokalt"

accessToken=$(printf 'n\n' | nais auth print-access-token --nais 2>/dev/null)

# Logg inn hvis output ikke inneholder gyldig JWT
if [[ ! "$accessToken" =~ ^eyJ[^.]+\.[^.]+\..+ ]]; then
  nais login --nais -y
fi

echo "Henter AzureAd secrets..."
nais secret get \
    "$(nais app env inntektsplanleggeren-backend-q2 --team ufore --environment dev-gcp --output json \
              | jq -r 'map(select(.name | startswith("AZURE_")) | .source.name) | unique[]')" \
    --team ufore --environment dev-gcp --with-values --reason "$reason" --output json | jq -r '.data[] | "\(.key)=\(.value)"' > $envFile

echo "Henter TokenX secrets..."
nais secret get \
    "$(nais app env inntektsplanleggeren-backend-q2 --team ufore --environment dev-gcp --output json \
              | jq -r 'map(select(.name | startswith("TOKEN_X_")) | .source.name) | unique[]')" \
      --team ufore --environment dev-gcp --with-values --reason "$reason" --output json | jq -r '.data[] | "\(.key)=\(.value)"' >> $envFile

echo "Lagret secrets i $envFile"
