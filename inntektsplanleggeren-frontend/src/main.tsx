import React from 'react'
import ReactDOM from 'react-dom/client'
import DataContextProvider from "./DataContextProvider"
import {AppRoutes} from "@/routes";

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
      <DataContextProvider>
          <div className="microfrontend-container content-wrapper">
              <representasjon-banner
                  representasjonstyper="PENSJON_FULLSTENDIG,PENSJON_BEGRENSET,UFORETYGD_SKRIV,UFORETYGD_KOMMUNISER,UFORETYGD_LES,PENSJON_SUPERADMIN"></representasjon-banner>
          </div>
          <AppRoutes/>
      </DataContextProvider>
  </React.StrictMode>
)
