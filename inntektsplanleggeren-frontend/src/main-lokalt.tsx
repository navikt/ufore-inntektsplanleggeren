import React from 'react'
import ReactDOM from 'react-dom/client'
import DataContextProvider from './context/DataContextProvider'
import { AppRoutes } from '@/routes'
import LokaltBanner from '@/components/common/Banner/LokaltBanner'

ReactDOM.createRoot(document.getElementById('root')!).render(
    <React.StrictMode>
        <DataContextProvider>
            <LokaltBanner>
                <AppRoutes />
            </LokaltBanner>
        </DataContextProvider>
    </React.StrictMode>
)
