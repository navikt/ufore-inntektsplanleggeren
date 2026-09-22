// Denne blir bytta ut av Nais når den deployes, og bundles ikke. Kun for lokal utvikling.
export default {
    // TODO: Sjekk om vi noen gang setter opp nais/tracing-demo i lokal utvikling, hvis ikke kan
    // vi fjerne denne linjen helt.
    // telemetryCollectorURL: 'http://localhost:12347/collect',
    app: {
        name: 'inntektsplanleggeren',
        version: 'dev',
    },
}
