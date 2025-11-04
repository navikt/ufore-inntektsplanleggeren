import './App.css'
import '@navikt/ds-css'
import { Heading } from '@navikt/ds-react'
import { Outlet, useLocation } from 'react-router-dom'
import { FormStateComponent } from '@/context/FormData'
import { useEffect } from 'react'
import VeilederBanner from '@/components/common/Banner/VeilederBanner'
import BorgerBanner from '@/components/common/Banner/BorgerBanner'

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
