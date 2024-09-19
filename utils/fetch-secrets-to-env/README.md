
Henter ut miljøvarabler fra kubernetes og legger de i kjøremiljøet

# Installer
- `./install.sh`

# Bruk

- Kjør `env-fetch-secrets` i terminal
- Bruk samme terminal til å kjøre applikasjon som trenger secrets


## Kjør med parametre
`env-fetch-secrets <namespace> <deployment> <secrets>`

- `namespace` - namespace i kubernetes
- `deployment` - deployment i kubernetes
- `secrets` - enten `azure`, `tokenx` eller `azure,tokenx`
