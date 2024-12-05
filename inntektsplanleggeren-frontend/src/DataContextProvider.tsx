import {createContext, useCallback, useEffect, useState} from "react";
import {
    getInitiate,
} from "@/api/apiFetching";
import {
    InitiateData, InitiateResponse,
    InntekterResponse,
    Message,
    SendApplicationResponse,
    SimulationResponse, StatusResponse
} from "@/api/model/ApiRequests";

export const InitialViewDefaultData: InitiateData | null = {
    forventetInntekt: 0,
    forventetInntektAnnenForelder: 0,
    inntektsgrense: 0,
    kompensasjonsgrad: 0,
    grenseStoppAvUfoeretrygd: 0,
    aktuelleAar: [],
    hasVarigTilrettelagtArbeid: false,
    hasBarneTilleggFellesbarn: false,
    grenseStoppAvBarnetilleggFellesbarn: null,
    fribelopBarnetilleggFellesbarn: null,
    hasBarnetilleggSaerkullsbarn: false,
    grenseStoppAvBarnetilleggSaerkullsbarn: null,
    fribelopBarnetilleggSaerkullsbarn: null,
    hasGjenlevendeTillegg: false
}

export const messagesDefaultValue: Message[]  = []


// export const WarningMessageDefaultValue: Message[] | null = []

interface DataContextValue {
    initiateResponse: InitiateResponse | null;
    setInitiateResponse: (value: InitiateResponse) => void;

    inntekterResponse: InntekterResponse | null;
    setInntekterResponse: (value: InntekterResponse) => void;

    simulationResponse: SimulationResponse | null;
    setSimulationResponse: (value: SimulationResponse) => void;

    sendResponse: SendApplicationResponse | null,
    setSendResponse: (value: SendApplicationResponse) => void,

    statusResponse: StatusResponse | null,
    setStatusResponse: (value: StatusResponse) => void,

    refetch: boolean;
    setRefetch: (value: boolean) => void;
    loading: boolean;
    setLoading: (loading: boolean) => void;
    error: boolean;
    setError: (value: boolean) => void;
    loadingError: boolean;
    setLoadingError: (value: boolean) => void;
    feilmeldingkode: string;
    setFeilmeldingkode: (value: string) => void;
    success: boolean;
    setSuccess: (value: boolean) => void;
}

const DataContextDefaultValue: DataContextValue = {
    initiateResponse: null,
    setInitiateResponse: () => undefined,

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
     
    loading: true,
    setLoading: () => undefined,
    error: false,
    setError: () => undefined,
    loadingError: false,
    setLoadingError: () => undefined,
    feilmeldingkode: "",
    setFeilmeldingkode: () => undefined,
    success: false,
    setSuccess: () => undefined
};


export const DataContext = createContext(DataContextDefaultValue);

interface DataContextProviderProps {
    children?: React.ReactNode;
}

function DataContextProvider(props: DataContextProviderProps) {
    const [refetch, setRefetch] = useState(DataContextDefaultValue.refetch)
    const [initiateResponse, setInitiateResponse] = useState(DataContextDefaultValue.initiateResponse)
    const [inntekterResponse, setInntekterResponse] = useState(DataContextDefaultValue.inntekterResponse)
    const [simulationResponse, setSimulationResponse] = useState(DataContextDefaultValue.simulationResponse)
    const [sendResponse, setSendResponse] = useState(DataContextDefaultValue.sendResponse)
    const [statusResponse, setStatusResponse] = useState(DataContextDefaultValue.statusResponse)
    const [loading, setLoading] = useState(DataContextDefaultValue.loading)
    const [error, setError] = useState(DataContextDefaultValue.error)
    const [loadingError, setLoadingError] = useState(DataContextDefaultValue.loadingError)
    const [feilmeldingkode, setFeilmeldingkode] = useState(DataContextDefaultValue.feilmeldingkode)
    const [success, setSuccess] = useState(DataContextDefaultValue.success)

    useCallback(function (res: boolean) {
        setRefetch(res)
    }, [setRefetch]);

    useCallback(function (res: boolean) {
        setError(res)
    }, [setError]);

    useCallback(function (res: boolean) {
        setLoadingError(res)
    }, [setLoadingError]);

    useEffect(() => {
            (async () => {
                if (refetch) {
                    try {
                        setLoading(true)

                        const inntektsPlanleggerenResponse = await getInitiate()
                        setInitiateResponse(inntektsPlanleggerenResponse)

                        setLoading(false)
                    } catch (e) {
                        setLoadingError(true)
                        setLoading(false)
                    }
                    setRefetch(false)
                }
            })();
        }
        , [refetch]);


    return (
        <DataContext.Provider value={{
            initiateResponse,
            setInitiateResponse,

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

            loading,
            setLoading,
            error,
            setError,
            loadingError,
            setLoadingError,
            feilmeldingkode,
            setFeilmeldingkode,
            success,
            setSuccess
        }}>
            {props.children}
        </DataContext.Provider>
    );

}

export default DataContextProvider