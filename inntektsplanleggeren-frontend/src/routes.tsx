import React, {useState} from "react";
import {Routes, Route, BrowserRouter} from "react-router-dom";
import {InitialView} from "@/components/initialView/InitialView";
import App from "@/App";
import {FormContainer} from "@/form-container";
import {Innfylling} from "@/components/innfylling/innfylling";
import {Beregning} from "@/components/beregning/beregning";
import {Oppsummering} from "@/components/oppsummering/Oppsummering";

export const AppRoutes = () => {
    const [counter, setCounter] = useState<number>(0);

    return (
        <BrowserRouter>
            <Routes>
                <Route element={<App />}>
                    <Route index element={<InitialView aktivSamboer={true}  setCounter={setCounter} availableYears={[2019, 2020]}/>} />
                    {/*<Route element={<AccessControl />}>*/}
                        <Route element={<FormContainer />}>
                            <Route path="/forventede-inntekter" element={<Innfylling />} />
                            <Route path="/beregning" element={<Beregning />} />
                            <Route path="/oppsummering" element={<Oppsummering />} />
                        </Route>
                    {/*</Route>*/}
                </Route>
            </Routes>
        </BrowserRouter>
    );
};
