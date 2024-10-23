import React, {createContext, SetStateAction, useContext, useState} from "react";
import {DataContext} from "@/DataContextProvider";
import {ForventedeInntekter, SimulationResult} from "@/api/model/ApiRequests";


interface FormState {
    formStep: number
    setFormStep: (value: SetStateAction<number>) => void
    selectedYear: string | null;
    setSelectedYear: (value: SetStateAction<string | null>) => void

    brukerinntekt: ForventedeInntekter
    setBrukerinntekt: (value: SetStateAction<ForventedeInntekter>) => void,

    annenForelderInntekt: ForventedeInntekter,
    setAnnenForelderInntekt: (value: SetStateAction<ForventedeInntekter>) => void,

    getBrukerinntektSum: () => number,
    getAnnenForelderInntektSum: () => number,

    simulationInntekt: SimulationResult | null,
    setSimulationInntekt: (value: SetStateAction<SimulationResult | null>) => void
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

    annenForelderInntekt: forventedeInntekterDefaultValue,
    setAnnenForelderInntekt: () => undefined,

    getBrukerinntektSum: () => 0,
    getAnnenForelderInntektSum: () => 0,
    formStep: 1,
    setFormStep: () => undefined,
    simulationInntekt: null,
    setSimulationInntekt: () => undefined
});

interface Props {
    children: React.ReactNode;
}



export const FormStateComponent = ({ children }: Props) => {
    const { initialViewData } = useContext(DataContext);
    const { aktuelleAar } = initialViewData;
    const [selectedYear, setSelectedYear]  = useState<string | null>(aktuelleAar.length === 1 ? aktuelleAar[0].toString(10) : null);
    const [brukerinntekt, setBrukerinntekt] = useState<ForventedeInntekter>(forventedeInntekterDefaultValue);
    const [annenForelderInntekt, setAnnenForelderInntekt] = useState<ForventedeInntekter>(forventedeInntekterDefaultValue);
    const [simulationInntekt, setSimulationInntekt] = useState<SimulationResult | null>(null);
    const [formStep, setFormStep] = useState<number>(1);

    const getBrukerinntektSum = () => Object.values(brukerinntekt).reduce((acc, val) => acc + val, 0);
    const getAnnenForelderInntektSum = () => Object.values(annenForelderInntekt).reduce((acc, val) => acc + val, 0);

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
            simulationInntekt,
            setSimulationInntekt,
        }}>
            {children}
        </FormStateContext.Provider>
    );
};
