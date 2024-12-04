import React, {createContext, SetStateAction, useState} from "react";
import { PersonInntekter } from "@/api/model/ApiRequests";

interface FormState {
    formStep: number | null;
    setFormStep: (value: SetStateAction<number | null>) => void;
    selectedYear: number | null;
    setSelectedYear: (value: SetStateAction<number | null>) => void;

    brukerinntekt: PersonInntekter;
    setBrukerinntekt: (value: SetStateAction<PersonInntekter>) => void;

    annenForelderInntekt: PersonInntekter | null;
    setAnnenForelderInntekt: (value: SetStateAction<PersonInntekter | null> | null) => void;

    getBrukerinntektSum: () => number;
    getAnnenForelderInntektSum: () => number | null;
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
    getAnnenForelderInntektSum: () => null,
    formStep: 1,
    setFormStep: () => undefined,
});

interface Props {
    children: React.ReactNode;
}


export const FormStateComponent = ({ children }: Props) => {
    const [selectedYear, setSelectedYear] = useState<number | null>(null);
    const [brukerinntekt, setBrukerinntekt] = useState<PersonInntekter>(forventedeInntekterDefaultValue);
    const [annenForelderInntekt, setAnnenForelderInntekt] = useState<PersonInntekter | null>(forventedeInntekterDefaultValue);
    const [formStep, setFormStep] = useState<number | null>(null);

    const getBrukerinntektSum = (): number => Object.values(brukerinntekt).reduce<number>((acc, val) => (acc ?? 0) + (val || 0), 0);
    const getAnnenForelderInntektSum = (): number | null => {
        if (!annenForelderInntekt) {
            return null;
        }
        const values = Object.values(annenForelderInntekt);
        const sum = values.reduce<number>((acc, val) => acc + (val || 0), 0);

        return sum;
};


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

