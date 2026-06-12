import { Theme } from '@navikt/ds-react'
import React from 'react'
import ReactDOM from 'react-dom/client'
import VeilederBanner from '@/components/common/Banner/VeilederBanner'
import { initFaro } from '@/faro'
import { AppRoutes } from '@/routes'
import DataContextProvider from './context/DataContextProvider'

initFaro()

ReactDOM.createRoot(document.getElementById('root')!).render(
    <React.StrictMode>
        <DataContextProvider>
            <Theme>
                <VeilederBanner />
                <AppRoutes />
            </Theme>
        </DataContextProvider>
    </React.StrictMode>
)
