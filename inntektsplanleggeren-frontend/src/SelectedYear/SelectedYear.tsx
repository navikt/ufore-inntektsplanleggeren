import React, {createContext, SetStateAction, useState} from "react";

interface FormState {
    selectedYear: number;
    setSelectedYear: (value: SetStateAction<number>) => void
}

export const FormStateContext = createContext<FormState>({
    selectedYear: -1,
    setSelectedYear: () => undefined
});

interface Props {
    children: React.ReactNode;
}

export const FormStateComponent = ({ children }: Props) => {
    const [year, setYear]  = useState(2019);

    return (
        <FormStateContext.Provider value={{ selectedYear: year, setSelectedYear: setYear }}>
            {children}
        </FormStateContext.Provider>
    );
};