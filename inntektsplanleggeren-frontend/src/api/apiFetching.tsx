import {
    ForventedeInntekter,
    InntekterResponse, SimulationRequest, SimulationResponse,
} from "@/api/model/ApiRequests";
import {mockInntekterResponse, mockInitiateResponse, InntektSimulationResponse} from "@/api/model/Mocks";

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

interface BaseInitiateData {
    forventetInntekt: number;
    forventetInntektAnnenForelder: number | null;
    inntektsgrense: number;
    kompensasjonsgrad: number;
    grenseStoppAvUfoeretrygd: number;
    aktuelleAar: number[];
    hasVarigTilrettelagtArbeid: boolean;
    hasGjenlevendeTillegg: boolean;
}

interface WithBarneTilleggFellesBarn extends BaseInitiateData {
    hasBarneTilleggFellesbarn: true;
    grenseStoppAvBarnetilleggFellesbarn: number;
    fribelopBarnetilleggFellesbarn: number;
}

interface WithoutBarneTilleggFellesBarn extends BaseInitiateData {
    hasBarneTilleggFellesbarn: false;
    grenseStoppAvBarnetilleggFellesbarn: null;
    fribelopBarnetilleggFellesbarn: null;
}

interface WithBarnetilleggSaerkullsbarn extends BaseInitiateData {
    hasBarnetilleggSaerkullsbarn: true,
    grenseStoppAvBarnetilleggSaerkullsbarn: number,
    fribelopBarnetilleggSaerkullsbarn: number,
}

interface WithoutBarnetilleggSaerkullsbarn extends BaseInitiateData {
    hasBarnetilleggSaerkullsbarn: false,
    grenseStoppAvBarnetilleggSaerkullsbarn: null,
    fribelopBarnetilleggSaerkullsbarn: null,
}

export type InitiateData = (WithBarneTilleggFellesBarn | WithoutBarneTilleggFellesBarn) & (WithBarnetilleggSaerkullsbarn | WithoutBarnetilleggSaerkullsbarn);

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

    return res.json();
}

export async function getInntekter(year: string): Promise<InntekterResponse> {
    const searchParams = new URLSearchParams(document.location.search)
    const pid: string | null = searchParams.get('pid')
    const headers = {
        'Content-Type': 'application/json',
        ...(pid && {'pid': pid})
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

    return res.json();
}

export async function submitInntektSimulation(brukerInntekter: ForventedeInntekter, epsInntekter: ForventedeInntekter | null, year: string): Promise<SimulationResponse> {
    const request: SimulationRequest = {
        bruker: brukerInntekter,
        eps: epsInntekter
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

    const res = await fetch(basePath + `/api/simuler?simuleringsaar=${year}`, {
        method: "POST",
        credentials: "include",
        headers: headers,
        body: JSON.stringify(request)
    });

    if (MOCKS_ENABLED) {
        return InntektSimulationResponse;
    }

    if (!res.ok) {
        throw new Error("Fikk ikke 2xx respons fra server");
    }

    return res.json();
}
