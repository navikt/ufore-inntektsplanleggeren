import React from 'react'
import ReactDOM from 'react-dom/client'
import DataContextProvider from './DataContextProvider'
import { AppRoutes } from '@/routes'

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <DataContextProvider>
      <AppRoutes />
    </DataContextProvider>
  </React.StrictMode>
)
