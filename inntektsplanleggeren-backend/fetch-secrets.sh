# Henter secrets for inntektsplanleggeren-backend og lagrer de under /private/tmp/ - her slettes alt ved restart av mac
# Sett opp 2run configuration" til å "Enable EnvFile" - og pek på rett fil
# Fungerer for mac.. (pga /private/tmp/)
currentDir=$(pwd)
cd /private/tmp/
node $currentDir/../utils/fetch-secrets-to-env/dist/fetch-secrets.js pensjonselvbetjening inntektsplanleggeren-backend-q2 azure,tokenx inntektsplanlegger.env
cd $currentDir
