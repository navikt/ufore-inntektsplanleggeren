import React, {useState} from "react";
import {Routes, Route, BrowserRouter} from "react-router-dom";
import {InitialView} from "@/components/initialView/InitialView";
import App from "@/App";
import {FormContainer} from "@/form-container";
import {Innfylling} from "@/components/innfylling/Innfylling";
import {Beregning} from "@/components/beregning/Beregning";
import {Oppsummering} from "@/components/oppsummering/Oppsummering";
import {Kvittering} from "@/components/kvittering/Kvittering";

export const AppRoutes = (
) => {
  //  const [displayData, setDisplayData] = useState<DisplayData>(DataContextProvider.)

    return (
        <BrowserRouter>
            <Routes>
                <Route element={<App />}>
                    <Route index element={<InitialView/>} />
                    {/*<Route element={<AccessControl />}>*/}
                        <Route element={<FormContainer />}>
                            <Route path="/forventede-inntekter" element={<Innfylling />} />
                            <Route path="/beregning" element={<Beregning />} />
                            <Route path="/oppsummering" element={<Oppsummering />} />
                            <Route path="/kvittering" element={<Kvittering />} />
                        </Route>
                    {/*</Route>*/}
                </Route>
            </Routes>
        </BrowserRouter>
    );
};
