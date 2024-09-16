import React, {createContext, SetStateAction, useState} from "react";
import {Link, Outlet} from "react-router-dom";
import {Button, FormProgress} from "@navikt/ds-react";

interface FormState {
    year: number;
    setYear: (value: SetStateAction<number>) => void
}

export const FormStateContext = createContext<FormState>({
    year: -1,
    setYear: () => undefined
});

export const FormContainer = () => {
    const [year, setYear]  = useState(2019);

    // const activeStep =

    return (
        <>
            <FormProgress totalSteps={3} activeStep={1}>
                <FormProgress.Step href="/forventede-inntekter" completed>Forventede inntekter</FormProgress.Step>
                <FormProgress.Step href="/beregning">Beregning</FormProgress.Step>
                <FormProgress.Step href="/oppsummering">Oppsummering før innsending</FormProgress.Step>
            </FormProgress>


            <FormStateContext.Provider value={{ year, setYear }}>
                <Outlet />
            </FormStateContext.Provider>
        </>
    );
};

