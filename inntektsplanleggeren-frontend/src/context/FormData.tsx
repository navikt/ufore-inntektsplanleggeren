import React, {createContext, SetStateAction, useState} from "react";
import {InntektInnfylling, PersonInntekt} from "@/api/apiFetching";


interface FormState {
    selectedYear?: string;
    setSelectedYear: (value: SetStateAction<string | undefined>) => void
    formData: InntektInnfylling;
    setFormData: (value: SetStateAction<InntektInnfylling>) => void,
    setPersoninntekt: (key: keyof PersonInntekt, value: number) => void,
    setAnnenForelderInntekt: (key: keyof PersonInntekt, value: number) => void,
    personInntektSum: number,
    annenForelderInntektSum: number,
    formStep: number
    setFormStep: (value: SetStateAction<number>) => void
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
    personInntektSum: 0,
    annenForelderInntektSum: 0,
    formStep: 1,
    setFormStep: () => undefined
});

interface Props {
    children: React.ReactNode;
}

function summerPersoninntekt(personInntekt: PersonInntekt) {
    return Object.values(personInntekt).reduce((acc, value) => acc + value, 0)
}

export const FormStateComponent = ({ children }: Props) => {
    const [year, setYear]  = useState<string | undefined>(undefined);
    const [formData, setFormData] = useState<InntektInnfylling>(initialFormData)
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
            personInntektSum: summerPersoninntekt(formData.personInntekt),
            annenForelderInntektSum: summerPersoninntekt(formData.annenForelderInntekt),
            formStep: formStep,
            setFormStep: setFormStep
        }}>
            {children}
        </FormStateContext.Provider>
    );
};