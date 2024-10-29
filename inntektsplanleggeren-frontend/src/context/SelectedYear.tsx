import React, {createContext, useContext} from "react";

interface SelectedYear {
    selectedYear: string;
}

export const SelectedYearContext = createContext<SelectedYear>({
    selectedYear: ""
});

interface Props {
    selectedYear: string;
    children: React.ReactNode;
}

export const SelectedYearProvider = ({ children, selectedYear }: Props) => (
    <SelectedYearContext.Provider value={{ selectedYear }}>
        {children}
    </SelectedYearContext.Provider>
);