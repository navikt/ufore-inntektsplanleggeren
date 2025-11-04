import React from 'react'
import ReactDOM from 'react-dom/client'
import DataContextProvider from './context/DataContextProvider'
import { AppRoutes } from '@/routes'
import VeilederBanner from '@/components/common/Banner/VeilederBanner'

ReactDOM.createRoot(document.getElementById('root')!).render(
    <React.StrictMode>
        <DataContextProvider>
            <VeilederBanner />
            <AppRoutes />
        </DataContextProvider>
    </React.StrictMode>
)
