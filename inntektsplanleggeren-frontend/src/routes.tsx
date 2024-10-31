import React, {useContext} from "react";
import {Routes, Route, BrowserRouter, Outlet, Navigate } from "react-router-dom";
import {InitialView} from "@/components/initialView/InitialView";
import App from "@/App";
import {FormContainer} from "@/form-container";
import {Innfylling} from "@/components/innfylling/Innfylling";
import {Oppsummering} from "@/components/oppsummering/Oppsummering";
import {Kvittering} from "@/components/kvittering/Kvittering";
import {FormStateContext} from "@/context/FormData";
import { SelectedYearProvider } from "@/context/SelectedYear";


export const BASE_PATH = "/pensjon/selvbetjening/inntektsplanleggeren";

export const AppRoutes = () => (
    <BrowserRouter basename={BASE_PATH}>
        <Routes>
            <Route element={<App />}>
                {/*<Route element={<AccessControl />}>*/}
                    <Route index element={<InitialView />} />
                    <Route element={<YearGuard />}>
                        <Route element={<FormContainer />}>
                            <Route index path={"/forventede-inntekter"} element={<Innfylling />} />
                            <Route index path={"/oppsummering"} element={<Oppsummering />} />
                            <Route index path={"/:id/kvittering"} element={<Kvittering />} />
                        </Route>
                    </Route>
                {/*</Route>*/}
            </Route>
        </Routes>
    </BrowserRouter>
);

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
