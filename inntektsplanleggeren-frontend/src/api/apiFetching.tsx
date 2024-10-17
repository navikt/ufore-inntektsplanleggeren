import {
    InntekterResponse, SubmitInntektRequest, SubmitInntektSimulationResponse,
} from "@/api/model/ApiRequests";
import {InntektSimulationDefaultValue} from "@/DataContextProvider";

export interface GetInntektResponse {
    messages: Message[]
    data: DisplayData

}

export interface Message {
    messageCode: string,
    details: string,
    type: string
}

export interface DisplayData {
    forventetInntekt: number
    forventetInntektAnnenForelder: number | null
    inntektsgrense: number
    kompensasjonsgrad: number
    grenseStoppAvUfoeretrygd: number
    aktuelleAar: number[]
    hasVarigTilrettelagtArbeid: boolean
    hasBarneTilleggFellesbarn: boolean,
    grenseStoppAvBarnetilleggFellesbarn: number | null,
    fribelopBarnetilleggFellesbarn: number | null,
    hasBarnetilleggSaerkullsbarn: boolean,
    grenseStoppAvBarnetilleggSaerkullsbarn: number | null,
    fribelopBarnetilleggSaerkullsbarn: number | null,
    hasGjenlevendeTillegg: boolean
}

export interface PersonInntekt {
    arbeidsinntekt: number
    navYtelse: number
    naeringsinntekt: number
    inntektFraUtlandet: number
    pensjonFraAndre: number
    pensjonFraUtlandet: number
}

export interface InntektInnfylling {
    personInntekt: PersonInntekt
    annenForelderInntekt: PersonInntekt
}

const inntektData : InntekterResponse = {
    "arbeidsinntektOgYtelserHittilIAar": [
        {
            "maned": 5,
            "belop": 53426.0,
            "inntektsgivere": [
                "Veterinær AS",
                "Grønnsakssuppekjøkkenet AS"
            ]
        },
        {
            "maned": 6,
            "belop": 0.0,
            "inntektsgivere": [
                // "Isbilen AS"
            ]
        },
        {
            "maned": 7,
            "belop": 54001.0,
            "inntektsgivere": [
                "Veterinær AS"
            ]
        },
        {
            "maned": 8,
            "belop": 7641.0,
            "inntektsgivere": [
                "Veterinær AS"
            ]
        }
    ],
    "pensjonFraAndreHittilIAar": [
        {
            "maned": 5,
            "belop": 10,
            "inntektsgivere": [
                "Isbilen AS",
                "Veterinær AS"
            ]
        },
        {
            "maned": 6,
            "belop": 34543.0,
            "inntektsgivere": [
                "Grønnsakssuppekjøkkenet AS"
            ]
        },
        {
            "maned": 7,
            "belop": 54001.0,
            "inntektsgivere": [
                "Isbilen AS"
            ]
        },
        {
            "maned": 8,
            "belop": 7641.0,
            "inntektsgivere": [
                "Isbilen AS"
            ]
        }
    ],
    "forventedeInntekter": {
        "bruker": {
            "arbeidsinntekt": {
                "belop": 32456
            },
            "andrePensjonsgivendeYtelser": {
                "belop": 22144
            },
            "naeringsinntekt": {
                "belop": 23543
            },
            "inntektUtland": {
                "belop": 1009
            },
            "pensjonUtland": {
                "belop": 9342
            }
        },
        "eps": {
            "arbeidsinntekt": {
                "belop": 10
            },
            "andrePensjonsgivendeYtelser": {
                "belop": 10
            },
            "naeringsinntekt": {
                "belop": 2341024
            },
            "inntektUtland": {
                "belop": 4553
            },
            "pensjonUtland": {
                "belop": 3323,
            }
        }
    },
    "uforeHeleAaret": false
};



export async function getInntektsgrense(): Promise<GetInntektResponse> {
    const searchParams = new URLSearchParams(document.location.search)
    const pid: string | null = searchParams.get('pid')

    let headers;

    if (pid) {
        headers =  {
            'Content-Type': 'application/json',
            'pid': pid
        }
    } else {
        headers = {
            'Content-Type': 'application/json'
        }
    }

    return await fetch(window.location.pathname + "api/initiate", {
        method: "GET",
        credentials: "include",
        headers: headers
    })
        .then(response => response.json())
        .then(response => {
            return response.displayData
        }).catch(() => {
            throw new Error("Fikk ikke 2xx respons fra server");
        })

}



const MOCKS_ENABLED = true;

export async function getInntekter(year: string): Promise<InntekterResponse> {
    const searchParams = new URLSearchParams(document.location.search)
    const pid: string | null = searchParams.get('pid')
    const headers = pid ? { 'Content-Type': 'application/json', 'pid': pid } : { 'Content-Type': 'application/json' };

    // return inntektData

    const res = await fetch(window.location.pathname + `api?year=${year}`, {
        method: "GET",
        credentials: "include",
        headers: headers
    });

    if (MOCKS_ENABLED) {
        return inntektData;
    }


    if (!res.ok) {

        throw new Error("Fikk ikke 2xx respons fra server");
    }

    const parsed = await res.json();

    return parsed.displayData;
}

export async function submitInntektSimulation(formData: InntektInnfylling, year: string): Promise<SubmitInntektSimulationResponse> {
    const request: SubmitInntektRequest = {
        inntekt: formData,
        year: year
    }
    const searchParams = new URLSearchParams(document.location.search)
    const pid: string | null = searchParams.get('pid')

    const headers = pid ? { 'Content-Type': 'application/json', 'pid': pid } : { 'Content-Type': 'application/json' };

    const res = await fetch(window.location.pathname + `api/inntektsplannleger`, {
        method: "POST",
        credentials: "include",
        headers: headers,
        body: JSON.stringify(request)
    });

    if (MOCKS_ENABLED) {
        return InntektSimulationDefaultValue;
    }


    if (!res.ok) {

        throw new Error("Fikk ikke 2xx respons fra server");
    }

    const parsed = await res.json();

    return parsed;
}





