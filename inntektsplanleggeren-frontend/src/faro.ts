import { getWebInstrumentations, initializeFaro, type TransportItem } from '@grafana/faro-web-sdk'

import nais from './nais.js'

export function initFaro() {
    if (typeof window === 'undefined') {
        return
    }

    try {
        initializeFaro({
            paused: window.location.hostname.includes('localhost'),
            url: nais.telemetryCollectorURL,
            app: nais.app,
            instrumentations: [...getWebInstrumentations()],
            beforeSend: (event: TransportItem) => {
                if (event.meta?.page?.url) {
                    const pageUrl = new URL(event.meta.page.url)
                    pageUrl.search = ''
                    event.meta.page.url = pageUrl.toString()
                }
                return event
            },
        })
    } catch (e) {
        console.error('Klarte ikke initialisere Faro: ', e)
        return null
    }
    return null
}
