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
    harVarigTilrettelagtArbeid: false,
    harBarneTillegg: true,
    harGjenlevendeTillegg: true
}

export const FamilierelasjonDefaultValue: Familierelasjon | null = {
    pid: "",
    relasjonstype: "",
    relasjonPersondata: {
        tilgangsbegrensning: null,
        navn: {
            fornavn: "",
            mellomnavn: null,
            etternavn: ""
        },
        foedselsdato: null,
        doedsdato: null,
    },
    fom: "",
    tom: ""
}

const SamboerforholdDefaultValue: Samboerforhold | null = {
    pid: "",
    fom: "",
    tom: "",
    periodeId: 0
}

const DataContextDefaultValue = {
    displayData: DisplayDataDefaultValue,
    refetch: true,
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    setRefetch: (value: boolean) => {
    },
    brukersEps: FamilierelasjonDefaultValue,
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    setBrukersEps: (value: React.SetStateAction<Familierelasjon>) => {
    },
    samboerforhold: [SamboerforholdDefaultValue],
    setSamboerforhold: {},
    historiskeForhold: [FamilierelasjonDefaultValue],
    setHistoriskeForhold: {},
    brukersBarn: [FamilierelasjonDefaultValue],
    setBrukersBarn: {},
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
    const [brukersEps, setBrukersEps] = useState(DataContextDefaultValue.brukersEps)
    const [samboerforhold, setSamboerforhold] = useState(DataContextDefaultValue.samboerforhold)
    const [historiskeForhold, setHistoriskeForhold] = useState(DataContextDefaultValue.historiskeForhold)
    const [brukersBarn, setBrukersBarn] = useState(DataContextDefaultValue.brukersBarn)
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
                    const searchParams = new URLSearchParams(document.location.search)
                    try {
                        setLoading(true)
                        const response: HenteFamilierelasjonsResponse = await hentEpsOgBarnForBruker(searchParams.get('pid'))
                        const epsOgBarn = response.familierelasjoner
                        setSamboerforhold(response.samboerforhold)
                        setBrukersEps(epsOgBarn.filter(familierelasjon => (familierelasjon.relasjonstype == EpsType.SAMBOER.toString()
                            || familierelasjon.relasjonstype == EpsType.EKTEFELLE.toString() || familierelasjon.relasjonstype == EpsType.PARTNER.toString()) && !familierelasjon.tom)[0])
                        setHistoriskeForhold(epsOgBarn.filter(familierelasjon => familierelasjon.relasjonstype == EpsType.SAMBOER.toString() && familierelasjon.tom))
                        setBrukersBarn(epsOgBarn.filter(familierelasjon => familierelasjon.relasjonstype == "BARN" && !familierelasjon.tom))

                        const inntektsPlannleggerResponse = await hentDisplayData()
                        setDisplayData(inntektsPlannleggerResponse)

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
            brukersEps,
            setBrukersEps,
            samboerforhold,
            setSamboerforhold,
            historiskeForhold,
            setHistoriskeForhold,
            brukersBarn,
            setBrukersBarn,
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