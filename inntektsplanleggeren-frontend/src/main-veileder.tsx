import React from 'react'
import ReactDOM from 'react-dom/client'
import DataContextProvider from './context/DataContextProvider'
import { AppRoutes } from '@/routes'
import VeilederBanner from '@/components/common/Banner/VeilederBanner'
import { Theme } from '@navikt/ds-react'

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
