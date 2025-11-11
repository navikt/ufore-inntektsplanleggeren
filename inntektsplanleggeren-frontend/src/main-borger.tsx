import React from 'react'
import ReactDOM from 'react-dom/client'
import DataContextProvider from './context/DataContextProvider'
import { AppRoutes } from '@/routes'
import BorgerBanner from '@/components/common/Banner/BorgerBanner'
import { Theme } from '@navikt/ds-react'

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
