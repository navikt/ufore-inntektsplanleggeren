import type { TransportItem } from '@grafana/faro-web-sdk'
import { fromNaisConfig, init, isLocalHost } from '@nais/apm'
import { enableApmReactRouterV6 } from '@nais/apm/react'
import { createRoutesFromChildren, matchRoutes, Routes, useLocation, useNavigationType } from 'react-router-dom'

import nais from './nais.js'

export function initApm() {
    if (typeof window === 'undefined') {
        return
    }

    const naisConfig = fromNaisConfig(nais)

    try {
        init({
            namespace: 'ufore',
            ...naisConfig,
            // Den lokale nais.js-fallbacken peker på en ekte URL (for en docker-compose-stack
            // vi ikke kjører), så vi utelater den eksplisitt på localhost for å få samme
            // console-echo-oppførsel lokalt som i andre miljøer uten kolletor.
            telemetryUrl: isLocalHost() ? undefined : naisConfig.telemetryUrl,
            beforeSend: (event: TransportItem) => {
                if (event.meta?.page?.url) {
                    const pageUrl = new URL(event.meta.page.url)
                    pageUrl.search = ''
                    event.meta.page.url = pageUrl.toString()
                }
                return event
            },
        })

        enableApmReactRouterV6({
            createRoutesFromChildren,
            matchRoutes,
            Routes,
            useLocation,
            useNavigationType,
        })
    } catch (e) {
        console.error('Klarte ikke initialisere @nais/apm: ', e)
    }
}
