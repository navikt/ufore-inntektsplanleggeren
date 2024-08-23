import React from 'react'
import ReactDOM from 'react-dom/client'
import DataContextProvider from "./DataContextProvider"
import App from "@/App"

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
      <DataContextProvider >
          <App />
      </DataContextProvider>
  </React.StrictMode>
)
