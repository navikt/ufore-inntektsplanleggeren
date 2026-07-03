import { Theme } from '@navikt/ds-react'
import React from 'react'
import ReactDOM from 'react-dom/client'
import LokaltBanner from '@/components/common/Banner/LokaltBanner'
import { initFaro } from '@/faro'
import { AppRoutes } from '@/routes'
import DataContextProvider from './context/DataContextProvider'

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
