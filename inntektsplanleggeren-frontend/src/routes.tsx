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
    const basePath = "/pensjon/selvbetjening/inntektsplanleggeren";

    return (
        <BrowserRouter>
            <Routes>
                <Route element={<App />}>
                    {/*<Route element={<AccessControl />}>*/}
                        <Route path={basePath} index element={<InitialView />} />
                        <Route element={<YearGuard />}>
                            <Route element={<FormContainer />}>
                                <Route path={basePath + "/forventede-inntekter"} element={<Innfylling />} />
                                <Route path={basePath + "/beregning"} element={<Oppsummering />} />
                                <Route path={basePath + "/oppsummering"} element={<Oppsummering />} />
                                <Route path={basePath + "/kvittering"} element={<Kvittering />} />
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

    if (selectedYear === null) {
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
