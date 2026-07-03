import { Theme } from '@navikt/ds-react'
import React from 'react'
import ReactDOM from 'react-dom/client'
import BorgerBanner from '@/components/common/Banner/BorgerBanner'
import { initFaro } from '@/faro'
import { AppRoutes } from '@/routes'
import DataContextProvider from './context/DataContextProvider'

initFaro()

ReactDOM.createRoot(document.getElementById('root')!).render(
    <React.StrictMode>
        <DataContextProvider>
            <Theme>
                <BorgerBanner />
                <AppRoutes />
            </Theme>
        </DataContextProvider>
    </React.StrictMode>
)
