import React, {useContext} from "react";
import {Outlet} from "react-router-dom";
import {FormProgress} from "@navikt/ds-react";
import {FormStateContext} from "@/context/FormData";



export const FormContainer = () => {


    const { formStep } = useContext(FormStateContext);

    return (
        <>
            <FormProgress totalSteps={3} activeStep={formStep}>
                <FormProgress.Step href="/forventede-inntekter" completed>Forventede inntekter</FormProgress.Step>
                <FormProgress.Step href="/beregning">Beregning</FormProgress.Step>
                <FormProgress.Step href="/oppsummering">Oppsummering før innsending</FormProgress.Step>
            </FormProgress>

            <Outlet/>
        </>
    );
};

