import {
    PersonInntekter, GetInntektsgrenseResponse,
    InntekterResponse, SubmitInntekterRequest, SimulationResponse, SendApplicationResponse, StatusResponse,
} from "@/api/model/ApiRequests";
import {
    mockInntekterResponse,
    mockInitiateResponse,
    mockSimulationResponse,
    mockSendApplicationResponse, mockStatusResponse
} from "@/api/model/Mocks";

const basePath = "/pensjon/selvbetjening/inntektsplanleggeren";

const MOCKS_ENABLED = true;

export async function getInitiate(): Promise<GetInntektsgrenseResponse> {
    const searchParams = new URLSearchParams(document.location.search)
    const pid: string | null = searchParams.get('pid')
    const headers = {
        'Content-Type': 'application/json',
        ...(pid && { 'pid': pid })
    };

    const res = await fetch(basePath + `/api/initiate`, {
        method: "GET",
        credentials: "include",
        headers: headers,
    });

    if (MOCKS_ENABLED) {
        return mockInitiateResponse
    }

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

    const res = await fetch(basePath + `/api/inntekter?simuleringsaar=${year}`, {
        method: "GET",
        credentials: "include",
        headers: headers
    });

    if (MOCKS_ENABLED) {
        return mockInntekterResponse
    }

    if (!res.ok) {
        console.log("error")
        throw new Error("Fikk ikke 2xx respons fra server");
    }

    return res.json();
}

export async function simulate(brukerInntekter: PersonInntekter, epsInntekter: PersonInntekter | null, year: string): Promise<SimulationResponse> {
    const request: SubmitInntekterRequest = {
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
        return mockSimulationResponse;
    }

    if (!res.ok) {
        throw new Error("Fikk ikke 2xx respons fra server");
    }

    return res.json();
}

export async function send(brukerInntekter: PersonInntekter, epsInntekter: PersonInntekter | null, year: string): Promise<SendApplicationResponse> {
    const request: SubmitInntekterRequest = {
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

    const res = await fetch(basePath + `/api/send?simuleringsaar=${year}`, {
        method: "POST",
        credentials: "include",
        headers: headers,
        body: JSON.stringify(request)
    });

    if (MOCKS_ENABLED) {
        return mockSendApplicationResponse;
    }

    if (!res.ok) {
        throw new Error("Fikk ikke 2xx respons fra server");
    }

    return res.json();
}

export async function getStatus(valgtaar: string, innsendingstidspunkt: Date): Promise<StatusResponse> {
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

    const res = await fetch(basePath + `/api/send?valgtaar=${valgtaar}&innsendingstidspunkt=${innsendingstidspunkt}`, {
        method: "POST",
        credentials: "include",
        headers: headers,
    });

    if (MOCKS_ENABLED) {
        return mockStatusResponse;
    }

    if (!res.ok) {
        throw new Error("Fikk ikke 2xx respons fra server");
    }

    return res.json();
}
