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
