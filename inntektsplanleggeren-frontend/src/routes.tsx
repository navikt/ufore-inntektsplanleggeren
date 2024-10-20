import React, {useContext} from "react";
import {Routes, Route, BrowserRouter, Outlet, Navigate} from "react-router-dom";
import {InitialView} from "@/components/initialView/InitialView";
import App from "@/App";
import {FormContainer} from "@/form-container";
import {Innfylling} from "@/components/innfylling/Innfylling";
import {Oppsummering} from "@/components/oppsummering/Oppsummering";
import {Kvittering} from "@/components/kvittering/Kvittering";
import {FormStateContext} from "@/context/FormData";
import { SelectedYearProvider } from "@/context/SelectedYear";

export const AppRoutes = (
) => {
  //  const [displayData, setDisplayData] = useState<DisplayData>(DataContextProvider.)

    return (
        <BrowserRouter>
            <Routes>
                <Route element={<App />}>
                    {/*<Route element={<AccessControl />}>*/}
                        <Route index element={<InitialView />} />
                        <Route element={<YearGuard />}>
                            <Route element={<FormContainer />}>
                                <Route path="/forventede-inntekter" element={<Innfylling />} />
                                <Route path="/beregning" element={<Oppsummering />} />
                                <Route path="/oppsummering" element={<Oppsummering />} />
                                <Route path="/kvittering" element={<Kvittering />} />
                            </Route>
                        </Route>
                    {/*</Route>*/}
                </Route>
            </Routes>
        </BrowserRouter>
    );
};

const YearGuard = () => {
    const { selectedYear } = useContext(FormStateContext);

    if (selectedYear === undefined) {
        return <Navigate to="/" replace />;
    }

    return (
        <SelectedYearProvider selectedYear={selectedYear}>
            <Outlet />
        </SelectedYearProvider>
    );
};

// const AccessControl = () => {
//     const user = getuser();
//
//     if (loading) {
//         return <Loader />;
//     }
//
//     if (user === undefined) {
//         return <Navigate to="/login"/>;
//     }
//
//     return <Outlet/>;
// };
