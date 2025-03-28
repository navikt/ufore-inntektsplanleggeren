import { createContext, useCallback, useEffect, useState } from 'react'
import { getInitiate } from '@/api/apiFetching'
import {
  InitiateResponse,
  InntekterResponse,
  SendApplicationResponse,
  SimulationResponse,
  StatusResponse,
} from '@/api/model/ApiRequests'
import { ErrorCode, ErrorResponse } from '@/components/common/Error'

interface DataContextValue {
  initiateResponse: InitiateResponse | null
  setInitiateResponse: (value: InitiateResponse) => void

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
  initiateResponse: null,
  setInitiateResponse: () => undefined,

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
  const [initiateResponse, setInitiateResponse] = useState(DataContextDefaultValue.initiateResponse)
  const [previousYearInntekterResponse, setPreviousYearInntekterResponse] = useState(
    DataContextDefaultValue.previousYearInntekterResponse
  )
  const [inntekterResponse, setInntekterResponse] = useState(DataContextDefaultValue.inntekterResponse)
  const [simulationResponse, setSimulationResponse] = useState(DataContextDefaultValue.simulationResponse)
  const [sendResponse, setSendResponse] = useState(DataContextDefaultValue.sendResponse)
  const [statusResponse, setStatusResponse] = useState(DataContextDefaultValue.statusResponse)
  const [errorMessage, setErrorMessage] = useState(DataContextDefaultValue.errorMessage)
  const [error, setError] = useState(DataContextDefaultValue.error)
  const [loadingError, setLoadingError] = useState(DataContextDefaultValue.loadingError)
  const [feilmeldingkode, setFeilmeldingkode] = useState(DataContextDefaultValue.feilmeldingkode)
  const [success, setSuccess] = useState(DataContextDefaultValue.success)

  useCallback(
    function (res: boolean) {
      setRefetch(res)
    },
    [setRefetch]
  )

  useCallback(
    function (res: boolean) {
      setError(res)
    },
    [setError]
  )

  useCallback(
    function (res: boolean) {
      setLoadingError(res)
    },
    [setLoadingError]
  )

  useEffect(() => {
    ;(async () => {
      if (refetch) {
        try {
          const inntektsPlanleggerenResponse = await getInitiate()
          if (inntektsPlanleggerenResponse instanceof ErrorResponse) {
            setErrorMessage(inntektsPlanleggerenResponse.message)
          } else {
            setInitiateResponse(inntektsPlanleggerenResponse)
          }
        } catch {
          setErrorMessage(ErrorCode.GENERIC_ERROR)
        }
        setRefetch(false)
      }
    })()
  }, [refetch])

  return (
    <DataContext.Provider
      value={{
        initiateResponse,
        setInitiateResponse,

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
