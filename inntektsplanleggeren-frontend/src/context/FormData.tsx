import React, {createContext, SetStateAction, useState} from "react";
import { PersonInntekter } from "@/api/model/ApiRequests";
import {useLocation} from "react-router-dom";

interface FormState {
    formStep: number;
    setFormStep: (value: SetStateAction<number>) => void;
    selectedYear: string | null;
    setSelectedYear: (value: SetStateAction<string | null>) => void;

    brukerinntekt: PersonInntekter;
    setBrukerinntekt: (value: SetStateAction<PersonInntekter>) => void;

    annenForelderInntekt: PersonInntekter | null;
    setAnnenForelderInntekt: (value: SetStateAction<PersonInntekter | null> | null) => void;

    getBrukerinntektSum: () => number;
    getAnnenForelderInntektSum: () => number;
}

const forventedeInntekterDefaultValue = {
    arbeidsinntekt: 0,
    andrePensjonsgivendeYtelser: 0,
    naeringsinntekt: 0,
    inntektUtland: 0,
    pensjonUtland: 0
};

export const FormStateContext = createContext<FormState>({
    selectedYear: null,
    setSelectedYear: () => undefined,
    brukerinntekt: forventedeInntekterDefaultValue,
    setBrukerinntekt: () => undefined,
    annenForelderInntekt: null,
    setAnnenForelderInntekt: () => undefined,
    getBrukerinntektSum: () => 0,
    getAnnenForelderInntektSum: () => 0,
    formStep: 1,
    setFormStep: () => undefined,
});

interface Props {
    children: React.ReactNode;
}

const YEAR_REGEX = /^\d{4}$/;

export const FormStateComponent = ({ children }: Props) => {
    const { state } = useLocation();
    const storedSelectedYear = state?.selectedYear ?? null;
    const [selectedYear, setSelectedYear] = useState<string | null>(typeof storedSelectedYear === 'string' && YEAR_REGEX.test(storedSelectedYear) ? storedSelectedYear : null);
    const [brukerinntekt, setBrukerinntekt] = useState<PersonInntekter>(forventedeInntekterDefaultValue);
    const [annenForelderInntekt, setAnnenForelderInntekt] = useState<PersonInntekter | null>(forventedeInntekterDefaultValue);
    const [formStep, setFormStep] = useState<number>(1);


    const getBrukerinntektSum = (): number => Object.values(brukerinntekt).reduce<number>((acc, val) => (acc ?? 0) + (val || 0), 0);
    const getAnnenForelderInntektSum = (): number => annenForelderInntekt ? Object.values(annenForelderInntekt).reduce<number>((acc, val) => (acc ?? 0) + (val || 0), 0) : 0;

    return (
        <FormStateContext.Provider value={{
            formStep,
            setFormStep,
            selectedYear,
            setSelectedYear,
            brukerinntekt,
            annenForelderInntekt,
            setBrukerinntekt,
            setAnnenForelderInntekt,
            getBrukerinntektSum,
            getAnnenForelderInntektSum,
        }}>
            {children}
        </FormStateContext.Provider>
    );
};