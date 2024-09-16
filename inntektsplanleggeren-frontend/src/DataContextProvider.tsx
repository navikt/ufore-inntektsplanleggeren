import {createContext, useCallback, useEffect, useState} from "react";
import {
    DisplayData,
    Familierelasjon, hentDisplayData,
    hentEpsOgBarnForBruker,
} from "@/api/apiFetching";
import {EpsType} from "@/components/types";
import {HenteFamilierelasjonsResponse, Samboerforhold} from "@/api/model/ApiRequests";

export const DisplayDataDefaultValue: DisplayData | null = {
    forventetInntekt: 100000,
    forventetInntektAnnenForelder: null,
    inntektsgrense: 50000,
    kompensasjonsgrad: 60.14,
    grenseStoppAvUfoeretrygd: 600000,
    aktuelleAar: [
        2024,
        2025
    ],
    harVarigTilrettelagtArbeid: true,
    harBarneTillegg: true,
    harGjenlevendeTillegg: true
}

// export const DisplayDataDefaultValue: DisplayData | null = {
//     forventetInntekt: 350000,
//     forventetInntektAnnenForelder: 250000,
//     inntektsgrense: 124028,
//     kompensasjonsgrad: 70.22,
//     grenseStoppAvUfoeretrygd: 559530,
//     aktuelleAar: [
//         2024
//     ],
//     harVarigTilrettelagtArbeid: true,
//     harBarneTillegg: true,
//     harGjenlevendeTillegg: false
// }

const DataContextDefaultValue = {
    displayData: DisplayDataDefaultValue,
    // initialWarningBox: null,
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
    // const [initialWarningVariable, setInitialWarningBox] = useState(DataContextDefaultValue.initialWarningBox)
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