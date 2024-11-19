import React, {createContext, SetStateAction, useEffect, useState} from "react";
import { PersonInntekter } from "@/api/model/ApiRequests";

interface FormState {
    formStep: number;
    setFormStep: (value: SetStateAction<number>) => void;
    selectedYear: number | null;
    setSelectedYear: (value: SetStateAction<number | null>) => void;

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

const SELECTED_YEAR_STORAGE_KEY = "selectedYear";

export const FormStateComponent = ({ children }: Props) => {
    const [selectedYear, setSelectedYear] = useState<number | null>(getStoredYear());
    const [brukerinntekt, setBrukerinntekt] = useState<PersonInntekter>(forventedeInntekterDefaultValue);
    const [annenForelderInntekt, setAnnenForelderInntekt] = useState<PersonInntekter | null>(forventedeInntekterDefaultValue);
    const [formStep, setFormStep] = useState<number>(1);

    useEffect(() => {
        if (selectedYear === null) {
            return;
        }
        
        sessionStorage.setItem(SELECTED_YEAR_STORAGE_KEY, selectedYear.toString(10));
    }, [selectedYear]);

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

const getStoredYear = (): number | null => {
    const storedYear = sessionStorage.getItem(SELECTED_YEAR_STORAGE_KEY);

    if (storedYear === null) {
        return null;
    }

    const parsed = Number.parseInt(storedYear, 10);

    return isNaN(parsed) ? null : parsed;
}