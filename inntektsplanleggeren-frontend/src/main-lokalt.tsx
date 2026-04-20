import React from 'react'
import ReactDOM from 'react-dom/client'
import DataContextProvider from './context/DataContextProvider'
import { AppRoutes } from '@/routes'
import LokaltBanner from '@/components/common/Banner/LokaltBanner'
import { Theme } from '@navikt/ds-react'
import { initFaro } from '@/faro'

initFaro()

ReactDOM.createRoot(document.getElementById('root')!).render(
    <React.StrictMode>
        <DataContextProvider>
            <Theme>
                <LokaltBanner>
                    <AppRoutes />
                </LokaltBanner>
            </Theme>
        </DataContextProvider>
    </React.StrictMode>
)
