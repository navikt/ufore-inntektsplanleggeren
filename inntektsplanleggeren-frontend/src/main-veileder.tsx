import { Theme } from '@navikt/ds-react'
import React from 'react'
import ReactDOM from 'react-dom/client'
import { initApm } from '@/apm'
import VeilederBanner from '@/components/common/Banner/VeilederBanner'
import { AppRoutes } from '@/routes'
import DataContextProvider from './context/DataContextProvider'

initApm()

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
