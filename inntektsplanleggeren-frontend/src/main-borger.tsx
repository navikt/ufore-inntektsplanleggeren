import { Theme } from '@navikt/ds-react'
import React from 'react'
import ReactDOM from 'react-dom/client'
import { initApm } from '@/apm'
import BorgerBanner from '@/components/common/Banner/BorgerBanner'
import { AppRoutes } from '@/routes'
import DataContextProvider from './context/DataContextProvider'

initApm()

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
