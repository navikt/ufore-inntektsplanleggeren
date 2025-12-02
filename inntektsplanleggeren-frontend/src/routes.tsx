import { useContext } from 'react'
import { Navigate, Outlet } from 'react-router-dom'
import { FormStateContext } from '@/context/FormData'
import { SelectedYearProvider } from '@/context/SelectedYear'
import { NedetidPage } from '@/components/nedetid/NedetidPage'

export const BASE_PATH = '/uforetrygd/selvbetjening/inntektsplanleggeren'

export const AppRoutes = () => {

    // TODO: få på plass feature toggling
    return <NedetidPage />

    // return (
    //     <BrowserRouter basename={BASE_PATH}>
    //         <Routes>
    //             <Route element={<App />}>
    //                 <Route index element={<InitialPage />} />
    //                 <Route index path={PageLinks.FORRIGE_INNTEKTER} element={<PreviousYearPage />} />
    //                 <Route element={<YearGuard />}>
    //                     <Route element={<FormContainer />}>
    //                         <Route index path={PageLinks.FORVENTET_INNTEKT} element={<InnfyllingPage />} />
    //                         <Route index path={PageLinks.BEREGNING} element={<BeregningPage />} />
    //                         <Route index path={PageLinks.OPPSUMMERING} element={<OppsummeringPage />} />
    //                         <Route index path={PageLinks.KVITTERING} element={<KvitteringPage />} />
    //                     </Route>
    //                 </Route>
    //             </Route>
    //         </Routes>
    //     </BrowserRouter>
    // )
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
