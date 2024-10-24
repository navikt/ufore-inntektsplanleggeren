import {createContext, useCallback, useEffect, useState} from "react";
import {
    InitiateData,
    getInntektsgrense,
    Message,
} from "@/api/apiFetching";
import {InntekterResponse, SimulationResponse} from "@/api/model/ApiRequests";

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


// export const WarningMessageDefaultValue: Message[] | null = [{
//     messageCode: "USER_HAS_NO_LOPENDE_VEDTAK_YET",
//     details: "Bruker kan ikke registrere inntektsendring før vedkommendes vedtak har blitt løpende",
//     type: "ERROR"
// }]

export const WarningMessageDefaultValue: Message[] | null = null

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
    warningMessage: Message[] | null;
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
    warningMessage: WarningMessageDefaultValue,
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
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    const [simulationResponse, setSimulationResponse] = useState(DataContextDefaultValue.simulationResponse)
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    const [warningMessage, setWarningMessage] = useState(DataContextDefaultValue.warningMessage)
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
            warningMessage,
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