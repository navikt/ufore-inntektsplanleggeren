import React, { createContext } from 'react'

interface SelectedYear {
  selectedYear: number
}

export const SelectedYearContext = createContext<SelectedYear>({
  selectedYear: 0,
})

interface Props {
  selectedYear: number
  children: React.ReactNode
}

export const SelectedYearProvider = ({ children, selectedYear }: Props) => (
  <SelectedYearContext.Provider value={{ selectedYear }}>{children}</SelectedYearContext.Provider>
)
