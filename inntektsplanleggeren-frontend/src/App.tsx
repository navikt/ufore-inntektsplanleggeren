import './App.css'
import '@navikt/ds-tokens'
import '@navikt/ds-css'
import { Heading } from '@navikt/ds-react'
import { useEffect } from 'react'
import { Outlet, useLocation } from 'react-router-dom'
import { FormStateComponent } from '@/context/FormData'

export function App() {
    const mode = import.meta.env.VITE_MODE

    console.log('Mode: ', mode)
    const location = useLocation()
    useEffect(() => {
        if (!location.hash) {
            window.scrollTo(0, 0)
        }
    }, [location])

    return (
        <FormStateComponent>
            <main id="maincontent" tabIndex={-1} className="mainBody">
                <div className="contentWrapper">
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
