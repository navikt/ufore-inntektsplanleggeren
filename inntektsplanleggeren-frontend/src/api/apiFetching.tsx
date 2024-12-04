import {
    PersonInntekter, GetInntektsgrenseResponse,
    InntekterResponse, SubmitInntekterRequest, SimulationResponse, SendApplicationResponse, StatusResponse
} from "@/api/model/ApiRequests";
import {
    mockInntekterResponse,
    mockInitiateResponse,
    mockSimulationResponse,
    mockSendApplicationResponse, mockStatusResponse
} from "@/api/model/Mocks";

const basePath = "/pensjon/selvbetjening/inntektsplanleggeren";

const MOCKS_ENABLED = false && import.meta.env.DEV;

export async function getInitiate(): Promise<GetInntektsgrenseResponse> {
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

    if (MOCKS_ENABLED) {
        return mockInitiateResponse
    }

    const res = await fetch(basePath + `/api/initiate`, {
        method: "GET",
        credentials: "include",
        headers: headers
    });


    if (!res.ok) {
        throw new Error("Fikk ikke 2xx respons fra server");
    }

    return res.json();
}



export async function getInntekter(year: number): Promise<InntekterResponse> {
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

export async function simulate(brukerInntekter: PersonInntekter, epsInntekter: PersonInntekter | null, year: number): Promise<SimulationResponse> {
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

    const request: SubmitInntekterRequest = {
        bruker: brukerInntekter,
        eps: epsInntekter
    }

    if (MOCKS_ENABLED) {
        return mockSimulationResponse;
    }

    const res = await fetch(basePath + `/api/simuler?simuleringsaar=${year}`, {
        method: "POST",
        credentials: "include",
        headers: headers,
        body: JSON.stringify(request)
    });

    if (!res.ok) {
        throw new Error("Fikk ikke 2xx respons fra server");
    }

    return res.json();
}

export async function send(brukerInntekter: PersonInntekter, epsInntekter: PersonInntekter | null, year: number): Promise<SendApplicationResponse> {
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

    const request: SubmitInntekterRequest = {
        bruker: brukerInntekter,
        eps: epsInntekter
    }

    if (MOCKS_ENABLED) {
        return mockSendApplicationResponse;
    }

    const res = await fetch(basePath + `/api/send?simuleringsaar=${year}`, {
        method: "POST",
        credentials: "include",
        headers: headers,
        body: JSON.stringify(request)
    });

    if (!res.ok) {
        throw new Error("Fikk ikke 2xx respons fra server");
    }

    return res.json();
}

export async function getStatus(valgtaar: number, innsendingstidspunkt: string): Promise<StatusResponse> {
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

    if (MOCKS_ENABLED) {
        return mockStatusResponse;
    }


    const url = encodeURI(basePath + `/api/status?valgtaar=${valgtaar}&innsendingstidspunkt=${innsendingstidspunkt}`)
    const res = await fetch(url, {
        method: "GET",
        credentials: "include",
        headers: headers,
    });

    if (!res.ok) {
        throw new Error("Fikk ikke 2xx respons fra server");
    }

    return res.json();
}
