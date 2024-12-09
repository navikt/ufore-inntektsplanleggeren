import React from 'react'
import ReactDOM from 'react-dom/client'
import DataContextProvider from "./DataContextProvider";
import {AppRoutes} from "@/routes";
import {InternalHeader} from "@navikt/ds-react";

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
      <DataContextProvider>
          <InternalHeader>
              <InternalHeader.Title as="h1">
                  Inntektsplanleggeren
              </InternalHeader.Title>
          </InternalHeader>
        <AppRoutes />
      </DataContextProvider>
  </React.StrictMode>
)
