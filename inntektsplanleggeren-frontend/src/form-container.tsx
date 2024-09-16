import React, {createContext, SetStateAction, useState} from "react";
import {Link, Outlet} from "react-router-dom";
import {Button, FormProgress} from "@navikt/ds-react";



export const FormContainer = () => {


    // const activeStep =

    return (
        <>
            <FormProgress totalSteps={3} activeStep={1}>
                <FormProgress.Step href="/forventede-inntekter" completed>Forventede inntekter</FormProgress.Step>
                <FormProgress.Step href="/beregning">Beregning</FormProgress.Step>
                <FormProgress.Step href="/oppsummering">Oppsummering før innsending</FormProgress.Step>
            </FormProgress>



                <Outlet />
        </>
    );
};

