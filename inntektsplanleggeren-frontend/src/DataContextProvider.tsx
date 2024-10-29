import {createContext, useCallback, useEffect, useState} from "react";
import {
    DisplayData,
    getInntektsgrense,
    Message,
} from "@/api/apiFetching";


export const InitialViewDefaultData: DisplayData | null = {
    forventetInntekt: 100000,
    forventetInntektAnnenForelder: null,
    inntektsgrense: 50000,
    kompensasjonsgrad: 60.14,
    grenseStoppAvUfoeretrygd: 600000,
    aktuelleAar: [
        2024,
        2023
    ],
    hasVarigTilrettelagtArbeid: true,
    hasBarneTillegg: true,
    hasGjenlevendeTillegg: true
}

export const WarningMessageDefaultValue: Message[] | null = [{
    messageCode: "USER_HAS_NO_LOPENDE_VEDTAK_YET",
    details: "Bruker kan ikke registrere inntektsendring før vedkommendes vedtak har blitt løpende",
    type: "ERROR"
}]

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


const DataContextDefaultValue = {
    initialViewData: InitialViewDefaultData,
    warningMessage: WarningMessageDefaultValue,
    refetch: true,
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    setRefetch: (value: boolean) => {
    },
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    loading: true,
    setLoading: {},
    error: false,
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    setError: (value: boolean) => {
    },
    loadingError: false,
    setLoadingError: {},
    feilmeldingkode: "",
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    setFeilmeldingkode: (value: string) => {
    },
    inntektSimulation: InntektSimulationDefaultValue,
    success: false,
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    setSuccess: (value: boolean) => {
    }
};


export const DataContext = createContext(DataContextDefaultValue);

interface DataContextProviderProps {
    children?: React.ReactNode;
}

function DataContextProvider(props: DataContextProviderProps) {
    const [refetch, setRefetch] = useState(DataContextDefaultValue.refetch)
    const [initialViewData, setInitialViewData] = useState(DataContextDefaultValue.initialViewData)
    const [inntektSimulation, setInntektSimulation] = useState(DataContextDefaultValue.inntektSimulation)
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
                        const inntektsPlannleggerResponse = await getInntektsgrense()
                        setInitialViewData(inntektsPlannleggerResponse.data)
                        // setInitialWarningBox(inntektsPlannleggerResponse.messages)

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
            refetch,
            setRefetch,
            initialViewData,
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