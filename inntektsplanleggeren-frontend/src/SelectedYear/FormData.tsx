import React, {createContext, SetStateAction, useState} from "react";
import {InntektInnfylling} from "@/api/apiFetching";

interface FormState {
    selectedYear?: string;
    setSelectedYear: (value: SetStateAction<string | undefined>) => void
    formData: InntektInnfylling | null;
    setFormData: (value: SetStateAction<InntektInnfylling>) => void
}

export const FormStateContext = createContext<FormState>({
    selectedYear: undefined,
    setSelectedYear: () => undefined,
    formData: null,
    setFormData: () => undefined,
    getSumFormDataPersonInntekt: (formData: InntektInnfylling) => sumFormDataPersonInntekt(formData),
    getSumFormDataPersonInntektAnnenForelder: (formData: InntektInnfylling) => sumFormDataPersonInntektAnnenForelder(formData),
});

interface Props {
    children: React.ReactNode;
}

function sumFormDataPersonInntekt(formData: InntektInnfylling) {
    return Object.values(formData.personInntekt).reduce((acc, value) => acc + value, 0)
}

function sumFormDataPersonInntektAnnenForelder(formData: InntektInnfylling) {
    return Object.values(formData.annenForelderInntekt).reduce((acc, value) => acc + value, 0)
}

export const FormStateComponent = ({ children }: Props) => {
    const [year, setYear]  = useState<string | undefined>(undefined);
    const [formData, setFormData] = useState<InntektInnfylling>(null);


    return (
        <FormStateContext.Provider value={{
            selectedYear: year,
            setSelectedYear: setYear,
            formData: formData,
            setFormData: setFormData,
        }}>
            {children}
        </FormStateContext.Provider>
    );
};