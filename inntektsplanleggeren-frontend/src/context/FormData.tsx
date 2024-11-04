import React, {createContext, SetStateAction, useState} from "react";
import {PersonInntekter} from "@/api/model/ApiRequests";


interface FormState {
    formStep: number
    setFormStep: (value: SetStateAction<number>) => void
    selectedYear: string | null;
    setSelectedYear: (value: SetStateAction<string | null>) => void

    brukerinntekt: PersonInntekter
    setBrukerinntekt: (value: SetStateAction<PersonInntekter>) => void,

    annenForelderInntekt: PersonInntekter | null,
    setAnnenForelderInntekt: (value: SetStateAction<PersonInntekter | null> | null) => void,

    getBrukerinntektSum: () => number,
    getAnnenForelderInntektSum: () => number | null,
}

const forventedeInntekterDefaultValue = {
    arbeidsinntekt: 0,
    andrePensjonsgivendeYtelser: 0,
    naeringsinntekt: 0,
    inntektUtland: 0,
    pensjonUtland: 0
}

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



export const FormStateComponent = ({ children }: Props) => {
    // const { initialViewData } = useContext(DataContext);
    // const { aktuelleAar } = initialViewData?.aktuelleAar || { aktuelleAar: [] };
    const [selectedYear, setSelectedYear]  = useState<string | null>(null);
    const [brukerinntekt, setBrukerinntekt] = useState<PersonInntekter>(forventedeInntekterDefaultValue);
    const [annenForelderInntekt, setAnnenForelderInntekt] = useState<PersonInntekter | null>(forventedeInntekterDefaultValue);
    const [formStep, setFormStep] = useState<number>(1);

    const getBrukerinntektSum = () => Object.values(brukerinntekt).reduce((acc, val) => acc + val, 0);
    const getAnnenForelderInntektSum = () => annenForelderInntekt ? Object.values(annenForelderInntekt).reduce((acc, val) => acc + val, 0) : null;

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
