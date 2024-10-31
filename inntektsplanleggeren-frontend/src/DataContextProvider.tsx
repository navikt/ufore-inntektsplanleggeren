import {createContext, useCallback, useEffect, useState} from "react";
import {
    getInntektsgrense,
} from "@/api/apiFetching";
import {InitiateData, InntekterResponse, Message, SimulationResponse} from "@/api/model/ApiRequests";

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

export const InntektSimulationDefaultValue = {
    "messages":[
        {
            "messageCode": "EPS_INNTEKT_CHANGED",
            "details": "Bruker har endret en av EPS sine inntekter sammenlignet med det som tidligere var benyttet som EPS sin inntekt.",
            "type": "WARNING",
            "metadata": {}
        }
    ],
    "result": {
        "uforetrygd": {
            "monthly": {"before": 0, "after": 0},
            "yearly": {"before": 0, "after": 0}
        },
        "forventetInntekt": {
            "monthly": {"before": 0, "after": 0},
            "yearly": {"before": 0, "after": 0}
        },
        "barnetilleggFellesbarn": {
            "monthly": {"before": 0, "after": 0},
            "yearly": {"before": 0, "after": 0}
        },
        "barnetilleggSaerkullsbarn": {
            "monthly": {"before": 0, "after": 0},
            "yearly": {"before": 0, "after": 0}
        },
        "gjenlevendetillegg": {
            "monthly": {"before": 0, "after": 0},
            "yearly": {"before": 0, "after": 0}
        },
        "sum": {
            "monthly": {"before": 0, "after": 0},
            "yearly": {"before": 0, "after": 0}
        }
    }
}

// export const WarningMessageDefaultValue: Message[] | null = []

interface DataContextValue {
    initialViewData: InitiateData;
    inntekterResponse: InntekterResponse | null;
    setInntekterResponse: (value: InntekterResponse) => void;
    simulationResponse: SimulationResponse;
    setSimulationResponse: (value: SimulationResponse) => void;
    messages: Message[];
    setMessages: (value: Message[]) => void;
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
    initialViewData: InitialViewDefaultData,
    inntekterResponse: null,
    setInntekterResponse: () => undefined,
    simulationResponse: InntektSimulationDefaultValue,
    setSimulationResponse: () => undefined,
    messages: messagesDefaultValue,
    setMessages: () => undefined,
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
    const [initialViewResponse, setInitialViewResponse] = useState(DataContextDefaultValue.initialViewData)
    const [inntekterResponse, setInntekterResponse] = useState(DataContextDefaultValue.inntekterResponse)
    const [simulationResponse, setSimulationResponse] = useState(DataContextDefaultValue.simulationResponse)
    const [messages, setMessages] = useState(DataContextDefaultValue.messages)
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
                        const inntektsPlanleggerenResponse = await getInntektsgrense()
                        setInitialViewResponse(inntektsPlanleggerenResponse.data)
                        setMessages(inntektsPlanleggerenResponse.messages)
                        // setInitialWarningBox(inntektsPlanleggerResponse.messages)

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
            initialViewData: initialViewResponse,
            inntekterResponse,
            setInntekterResponse,
            simulationResponse,
            setSimulationResponse,
            refetch,
            setRefetch,
            messages,
            setMessages,
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