import {createContext, useCallback, useEffect, useState} from "react";
import {
    InitiateData,
    getInntektsgrense,
    Message,
} from "@/api/apiFetching";
import {InntekterResponse} from "@/api/model/ApiRequests";


// export const InitialViewDefaultData: DisplayData | null = {
//     forventetInntekt: 120000,
//     forventetInntektAnnenForelder: 800000,
//     inntektsgrense: 49611,
//     kompensasjonsgrad: 49.7,
//     grenseStoppAvUfoeretrygd: 460060,
//     aktuelleAar: [
//         2024,
//         2025
//     ],
//     hasVarigTilrettelagtArbeid: true,
//     hasBarneTilleggFellesbarn: true,
//     grenseStoppAvBarnetilleggFellesbarn: 669751,
//     fribelopBarnetilleggFellesbarn: 570529,
//     hasBarnetilleggSaerkullsbarn: false,
//     grenseStoppAvBarnetilleggSaerkullsbarn: 676,
//     fribelopBarnetilleggSaerkullsbarn: 576,
//     hasGjenlevendeTillegg: true
// }

export const InitialViewDefaultData: InitiateData | null = {
    forventetInntekt: 0,
    forventetInntektAnnenForelder: 0,
    inntektsgrense: 0,
    kompensasjonsgrad: 0,
    grenseStoppAvUfoeretrygd: 0,
    aktuelleAar: [],
    hasVarigTilrettelagtArbeid: true,
    hasBarneTilleggFellesbarn: true,
    grenseStoppAvBarnetilleggFellesbarn: 669751,
    fribelopBarnetilleggFellesbarn: 570529,
    hasBarnetilleggSaerkullsbarn: false,
    grenseStoppAvBarnetilleggSaerkullsbarn: 676,
    fribelopBarnetilleggSaerkullsbarn: 576,
    hasGjenlevendeTillegg: true
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
        "uforetrygd": {"before": 200000, "after": 300000},
        "forventetInntekt": {"before": 430982, "after": 150000},
        "barnetilleggFellesbarn": {"before": 0, "after": 4342},
        "barnetilleggSaerkullsbarn": {"before": 0, "after": 0},
        "gjenlevendetillegg": {"before": 0, "after": 0},
        "sum": {"before": 630982, "after": 454342},
    }
}

// export const WarningMessageDefaultValue: Message[] | null = []

interface DataContextValue {
    initialViewData: InitiateData;
    inntekterResponse: InntekterResponse | null;
    setInntekterResponse: (value: InntekterResponse) => void;
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
    inntektSimulation: typeof InntektSimulationDefaultValue;
    success: boolean;
    setSuccess: (value: boolean) => void;
}

const DataContextDefaultValue: DataContextValue = {
    initialViewData: InitialViewDefaultData,
    inntekterResponse: null,
    setInntekterResponse: () => undefined,
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
    inntektSimulation: InntektSimulationDefaultValue,
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
    const [inntektSimulation, setInntektSimulation] = useState(DataContextDefaultValue.inntektSimulation)
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
            inntektSimulation,
            inntekterResponse,
            setInntekterResponse,
            refetch,
            setRefetch,
            initialViewData: initialViewResponse,
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