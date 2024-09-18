import {createContext, useCallback, useEffect, useState} from "react";
import {
    DisplayData,
    hentDisplayData,
    Message,
} from "@/api/apiFetching";


export const DisplayDataDefaultValue: DisplayData | null = {
    forventetInntekt: 100000,
    forventetInntektAnnenForelder: null,
    inntektsgrense: 50000,
    kompensasjonsgrad: 60.14,
    grenseStoppAvUfoeretrygd: 600000,
    aktuelleAar: [
        2024,
        2023
    ],
    harVarigTilrettelagtArbeid: true,
    harBarneTillegg: true,
    harGjenlevendeTillegg: true
}

export const WarningMessageDefaultValue: Message[] | null = [{
    messageCode: "USER_HAS_NO_LOPENDE_VEDTAK_YET",
    details: "Bruker kan ikke registrere inntektsendring før vedkommendes vedtak har blitt løpende",
    type: "ERROR"
}]

// export const WarningMessageDefaultValue: Message[] | null = []


const DataContextDefaultValue = {
    displayData: DisplayDataDefaultValue,
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
    const [displayData, setDisplayData] = useState(DataContextDefaultValue.displayData)
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
                        const inntektsPlannleggerResponse = await hentDisplayData()
                        setDisplayData(inntektsPlannleggerResponse.data)
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
            refetch,
            setRefetch,
            displayData,
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