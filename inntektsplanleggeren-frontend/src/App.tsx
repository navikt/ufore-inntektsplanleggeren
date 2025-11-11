import './App.css'
import '@navikt/ds-tokens'
import '@navikt/ds-css/darkside'
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
