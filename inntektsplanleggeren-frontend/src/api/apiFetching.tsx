import {
    EndreSamboerforholdRequest,
    RelasjonPersondata, SubmitInntektRequest,
} from "@/api/model/ApiRequests";

export interface Familierelasjon {
    pid: string,
    fom: string | undefined,
    tom: string | undefined,
    relasjonstype: string,
    relasjonPersondata: RelasjonPersondata | undefined
}

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
    harVarigTilrettelagtArbeid: boolean
    harBarneTillegg: boolean
    harGjenlevendeTillegg: boolean
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

export async function hentDisplayData(): Promise<GetInntektResponse> {
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

    return await fetch(window.location.pathname + "api/inntektsplannleger", { //todo fix url
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

import { ApiResponse } from "@/api/model/ApiRequests";

export const fetchInntekter = async (year: number): Promise<ApiResponse> => {
    const response = await fetch(`/api/inntekter?year=${year}`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        }
    });

    if (!response.ok) {
        throw new Error('Network response was not ok');
    }

    const data: ApiResponse = await response.json();
    return data;
};

