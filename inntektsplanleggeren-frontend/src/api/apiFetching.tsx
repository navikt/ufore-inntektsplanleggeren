import {
    ForventedeInntekter,
    InntekterResponse, SubmitInntektRequest, SubmitInntektSimulationResponse,
} from "@/api/model/ApiRequests";
import {InntektSimulationDefaultValue} from "@/DataContextProvider";
import {mockInntekterResponse, mockInitiateResponse} from "@/api/model/Mocks";

const basePath = "/pensjon/selvbetjening/inntektsplanleggeren";

const MOCKS_ENABLED = true;

export interface GetInntektsgrenseResponse {
    messages: Message[]
    data: InitiateData
}

export interface Message {
    messageCode: string,
    details: string,
    type: string
    metadata: StringDictionary
}

export interface StringDictionary {
    [key: string]: never;
}

export interface InitiateData {
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
    andrePensjonsgivendeYtelser: number
    naeringsinntekt: number
    inntektUtland: number
    pensjonUtland: number
}

export interface InntektInnfylling {
    brukerinntekt: ForventedeInntekter
    annenForelderInntekt: ForventedeInntekter
}


export async function getInntektsgrense(): Promise<GetInntektsgrenseResponse> {
    const searchParams = new URLSearchParams(document.location.search)
    const pid: string | null = searchParams.get('pid')
    const headers = {
        'Content-Type': 'application/json',
        ...(pid && { 'pid': pid })
    };

    if (MOCKS_ENABLED) {
        return mockInitiateResponse
    }

    const res = await fetch(basePath + `/api/initiate`, {
        method: "GET",
        credentials: "include",
        headers: headers,
    });

    if (!res.ok) {
        throw new Error("Fikk ikke 2xx respons fra server");
    }

    const parsed = await res.json();
    return parsed;
}

export async function getInntekter(year: string): Promise<InntekterResponse> {
    const searchParams = new URLSearchParams(document.location.search)
    const pid: string | null = searchParams.get('pid')
    const headers = {
        'Content-Type': 'application/json',
        ...(pid && { 'pid': pid })
    };

    if (MOCKS_ENABLED) {
        return mockInntekterResponse
    }

    const res = await fetch(basePath + `/api/inntekter?simuleringsaar=${year}`, {
        method: "GET",
        credentials: "include",
        headers: headers
    });

    if (!res.ok) {
        console.log("error")
        throw new Error("Fikk ikke 2xx respons fra server");
    }

    const parsed = await res.json();
    return parsed;
}

export async function submitInntektSimulation(formData: InntektInnfylling, year: string): Promise<SubmitInntektSimulationResponse> {
    const request: SubmitInntektRequest = {
        inntekt: formData,
        year: year
    }
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

    const res = await fetch(basePath + `api/inntektsplanlegger`, {
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





