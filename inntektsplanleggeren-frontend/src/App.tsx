import './App.css'
import '@navikt/ds-css'
import { Heading } from '@navikt/ds-react'
import { Outlet, useLocation } from 'react-router-dom'
import { FormStateComponent } from '@/context/FormData'
import { useEffect } from 'react'

export function App() {
  const location = useLocation()
  useEffect(() => {
    if (!location.hash) {
      window.scrollTo(0, 0)
    }
  }, [location])

  return (
    <FormStateComponent>
      <main className="mainBody">
        <div className="contentWrapper">
          <representasjon-banner
            representasjonstyper="PENSJON_FULLSTENDIG,PENSJON_BEGRENSET,UFORETRYGD_SKRIV,UFORETRYGD_KOMMUNISER,UFORETRYGD_LES,PENSJON_SUPERADMIN"
            className="representasjon-banner"
            redirectTo={`${window.location.origin}/uforetrygd/selvbetjening/inntektsplanleggeren`}
          ></representasjon-banner>
          <Heading size="xlarge" level="1" className="main-header">
            Inntektsplanleggeren
          </Heading>
          <Outlet />
        </div>
      </main>
    </FormStateComponent>
  )
}

export default App
