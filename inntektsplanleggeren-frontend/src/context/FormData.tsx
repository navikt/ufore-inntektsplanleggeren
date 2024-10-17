import React, {createContext, SetStateAction, useContext, useState} from "react";
import {InntektInnfylling, PersonInntekt} from "@/api/apiFetching";
import {DataContext} from "@/DataContextProvider";
import {SimulationResult} from "@/api/model/ApiRequests";


interface FormState {
    formStep: number
    setFormStep: (value: SetStateAction<number>) => void
    selectedYear?: string;
    setSelectedYear: (value: SetStateAction<string | undefined>) => void

    formData: InntektInnfylling;
    setFormData: (value: SetStateAction<InntektInnfylling>) => void,
    setPersoninntekt: (key: keyof PersonInntekt, value: number) => void,
    setAnnenForelderInntekt: (key: keyof PersonInntekt, value: number) => void,
    getPersonInntektSum: number,
    getAnnenForelderInntektSum: number,

    simulationInntekt: SimulationResult | undefined,
    setSimulationInntekt: (value: SetStateAction<SimulationResult | undefined>) => void
}

const initialFormData = {
    personInntekt: {
        arbeidsinntekt: 0,
        navYtelse: 0,
        naeringsinntekt: 0,
        inntektFraUtlandet: 0,
        pensjonFraAndre: 0,
        pensjonFraUtlandet: 0
    },
    annenForelderInntekt: {
        arbeidsinntekt: 0,
        navYtelse: 0,
        naeringsinntekt: 0,
        inntektFraUtlandet: 0,
        pensjonFraAndre: 0,
        pensjonFraUtlandet: 0
    }
}

export const FormStateContext = createContext<FormState>({
    selectedYear: undefined,
    setSelectedYear: () => undefined,
    formData: initialFormData,
    setFormData: () => undefined,
    setPersoninntekt: () => undefined,
    setAnnenForelderInntekt: () => undefined,
    getPersonInntektSum: 0,
    getAnnenForelderInntektSum: 0,
    formStep: 1,
    setFormStep: () => undefined,
    simulationInntekt: undefined,
    setSimulationInntekt: () => undefined
});

interface Props {
    children: React.ReactNode;
}

function summerPersoninntekt(personInntekt: PersonInntekt) {
    return Object.values(personInntekt).reduce((acc, value) => acc + value, 0)
}

export const FormStateComponent = ({ children }: Props) => {
    const { initialViewData } = useContext(DataContext)
    const { aktuelleAar } = initialViewData;
    const [year, setYear]  = useState<string | undefined>(aktuelleAar.length === 1 ? aktuelleAar[0].toString(10) : undefined);
    const [formData, setFormData] = useState<InntektInnfylling>(initialFormData)
    const [simulationInntekt, setSimulationInntekt] = useState<SimulationResult | undefined>();
    const [formStep, setFormStep] = useState<number>(1);

    const setPersoninntekt = (key: keyof PersonInntekt, value: number) => {
        setFormData((prev) => ({
            ...prev,
            personInntekt: {
                ...prev.personInntekt,
                [key]: value,
            },
        }));
    };

    const setAnnenForelderInntekt = (key: keyof PersonInntekt, value: number) => {
        setFormData((prev) => ({
            ...prev,
            annenForelderInntekt: {
                ...prev.annenForelderInntekt,
                [key]: value,
            },
        }));
    }


    return (
        <FormStateContext.Provider value={{
            selectedYear: year,
            setSelectedYear: setYear,
            formData: formData,
            setFormData: setFormData,
            setPersoninntekt: setPersoninntekt,
            setAnnenForelderInntekt: setAnnenForelderInntekt,
            getPersonInntektSum: summerPersoninntekt(formData.personInntekt),
            getAnnenForelderInntektSum: summerPersoninntekt(formData.annenForelderInntekt),
            formStep: formStep,
            setFormStep: setFormStep,
            simulationInntekt: simulationInntekt,
            setSimulationInntekt: setSimulationInntekt
        }}>
            {children}
        </FormStateContext.Provider>
    );
};