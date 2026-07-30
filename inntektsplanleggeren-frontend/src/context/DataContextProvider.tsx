import { createContext, useCallback, useState } from 'react'
import type { InntekterResponse, SendApplicationResponse, SimulationResponse, StatusResponse } from '@/api/model/ApiRequests'
import type { ErrorCode } from '@/components/common/Error'

interface DataContextValue {
    previousYearInntekterResponse: InntekterResponse | null
    setPreviousYearInntekterResponse: (value: InntekterResponse) => void

    inntekterResponse: InntekterResponse | null
    setInntekterResponse: (value: InntekterResponse) => void

    simulationResponse: SimulationResponse | null
    setSimulationResponse: (value: SimulationResponse) => void

    sendResponse: SendApplicationResponse | null
    setSendResponse: (value: SendApplicationResponse) => void

    statusResponse: StatusResponse | null
    setStatusResponse: (value: StatusResponse) => void

    refetch: boolean
    setRefetch: (value: boolean) => void

    error: boolean
    setError: (value: boolean) => void
    errorMessage: ErrorCode | null
    setErrorMessage: (value: ErrorCode) => void
    loadingError: boolean
    setLoadingError: (value: boolean) => void
    feilmeldingkode: string
    setFeilmeldingkode: (value: string) => void
    success: boolean
    setSuccess: (value: boolean) => void
}

const DataContextDefaultValue: DataContextValue = {
    previousYearInntekterResponse: null,
    setPreviousYearInntekterResponse: () => undefined,

    inntekterResponse: null,
    setInntekterResponse: () => undefined,

    simulationResponse: null,
    setSimulationResponse: () => undefined,

    sendResponse: null,
    setSendResponse: () => undefined,

    statusResponse: null,
    setStatusResponse: () => undefined,

    refetch: true,
    setRefetch: () => undefined,

    error: false,
    setError: () => undefined,
    errorMessage: null,
    setErrorMessage: () => undefined,
    loadingError: false,
    setLoadingError: () => undefined,
    feilmeldingkode: '',
    setFeilmeldingkode: () => undefined,
    success: false,
    setSuccess: () => undefined,
}

export const DataContext = createContext(DataContextDefaultValue)

interface DataContextProviderProps {
    children?: React.ReactNode
}

function DataContextProvider(props: DataContextProviderProps) {
    const [refetch, setRefetch] = useState(DataContextDefaultValue.refetch)
    const [previousYearInntekterResponse, setPreviousYearInntekterResponse] = useState(DataContextDefaultValue.previousYearInntekterResponse)
    const [inntekterResponse, setInntekterResponse] = useState(DataContextDefaultValue.inntekterResponse)
    const [simulationResponse, setSimulationResponse] = useState(DataContextDefaultValue.simulationResponse)
    const [sendResponse, setSendResponse] = useState(DataContextDefaultValue.sendResponse)
    const [statusResponse, setStatusResponse] = useState(DataContextDefaultValue.statusResponse)
    const [errorMessage, setErrorMessage] = useState(DataContextDefaultValue.errorMessage)
    const [error, setError] = useState(DataContextDefaultValue.error)
    const [loadingError, setLoadingError] = useState(DataContextDefaultValue.loadingError)
    const [feilmeldingkode, setFeilmeldingkode] = useState(DataContextDefaultValue.feilmeldingkode)
    const [success, setSuccess] = useState(DataContextDefaultValue.success)

    useCallback((res: boolean) => {
        setRefetch(res)
    }, [])

    useCallback((res: boolean) => {
        setError(res)
    }, [])

    useCallback((res: boolean) => {
        setLoadingError(res)
    }, [])

    return (
        <DataContext.Provider
            value={{
                previousYearInntekterResponse,
                setPreviousYearInntekterResponse,
                inntekterResponse,
                setInntekterResponse,
                simulationResponse,
                setSimulationResponse,
                sendResponse,
                setSendResponse,
                statusResponse,
                setStatusResponse,
                refetch,
                setRefetch,
                error,
                setError,
                errorMessage,
                setErrorMessage,
                loadingError,
                setLoadingError,
                feilmeldingkode,
                setFeilmeldingkode,
                success,
                setSuccess,
            }}
        >
            {props.children}
        </DataContext.Provider>
    )
}

export default DataContextProvider
