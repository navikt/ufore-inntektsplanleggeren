# env-fetch-secrets
Henter ut miljøvarabler fra kubernetes og legger de i kjøremiljøet

## Installer

- `./install.sh`

## Bruk

- Kjør `env-fetch-secrets` i terminal
- Bruk samme terminal til å kjøre applikasjon som trenger secrets

### Kjør med parametre

`env-fetch-secrets <namespace> <deployment> <secrets>`

- `namespace` - namespace i kubernetes
- `deployment` - deployment i kubernetes
- `secrets` - enten `azure`, `tokenx` eller `azure,tokenx`

Parametere er optional, så om du bare legger til `namespace` så får du prompt om `deployment` og `secrets`

### Lagre til envfil

`env-fetch-secrets <namespace> <deployment> <secrets> <navn på fil>`

Kan bare brukes med alle parametre. `<navn på fil>` er en fil som blir lagret i mappen scriptet kjøres i
