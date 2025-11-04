import React from 'react'
import ReactDOM from 'react-dom/client'
import DataContextProvider from './context/DataContextProvider'
import { AppRoutes } from '@/routes'
import BorgerBanner from '@/components/common/Banner/BorgerBanner'

ReactDOM.createRoot(document.getElementById('root')!).render(
    <React.StrictMode>
        <DataContextProvider>
            <BorgerBanner />
            <AppRoutes />
        </DataContextProvider>
    </React.StrictMode>
)
