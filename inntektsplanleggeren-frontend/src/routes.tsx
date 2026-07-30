import { useContext } from 'react'
import { BrowserRouter, Navigate, Outlet, Route, Routes } from 'react-router-dom'
import App from '@/App'
import { BeregningPage } from '@/components/beregning/BeregningPage'
import { InnfyllingPage } from '@/components/innfylling/InnfyllingPage'
import { KvitteringPage } from '@/components/kvittering/KvitteringPage'
import { NedetidPage } from '@/components/nedetid/NedetidPage'
import { OppsummeringPage } from '@/components/oppsummering/OppsummeringPage'
import { PreviousYearPage } from '@/components/previousYear/PreviousYearPage'
import { Startside } from '@/components/startside/Startside'
import { FormStateContext } from '@/context/FormData'
import { SelectedYearProvider } from '@/context/SelectedYear'
import { FormContainer, PageLinks } from '@/FormContainer'
import { useToggle } from '@/hooks/useToggle'

export const BASE_PATH = '/uforetrygd/selvbetjening/inntektsplanleggeren'

export const AppRoutes = () => {
    const nedetid = useToggle('inntektsplanleggeren.nedetid')

    if (nedetid) return <NedetidPage />

    return (
        <BrowserRouter basename={BASE_PATH}>
            <Routes>
                <Route element={<App />}>
                    <Route index element={<Startside />} />
                    <Route index path={PageLinks.FORRIGE_INNTEKTER} element={<PreviousYearPage />} />
                    <Route element={<YearGuard />}>
                        <Route element={<FormContainer />}>
                            <Route index path={PageLinks.FORVENTET_INNTEKT} element={<InnfyllingPage />} />
                            <Route index path={PageLinks.BEREGNING} element={<BeregningPage />} />
                            <Route index path={PageLinks.OPPSUMMERING} element={<OppsummeringPage />} />
                            <Route index path={PageLinks.KVITTERING} element={<KvitteringPage />} />
                        </Route>
                    </Route>
                </Route>
            </Routes>
        </BrowserRouter>
    )
}

const YearGuard = () => {
    const { selectedYear } = useContext(FormStateContext)

    if (selectedYear === null || selectedYear === 0) {
        return <Navigate to="/" replace />
    }

    return (
        <SelectedYearProvider selectedYear={selectedYear}>
            <Outlet />
        </SelectedYearProvider>
    )
}
