import {
    PersonInntekter, GetInntektsgrenseResponse,
    InntekterResponse, SubmitInntekterRequest, SimulationResponse, SendApplicationResponse, StatusResponse, FormState,
} from "@/api/model/ApiRequests";
import {
    mockInntekterResponse,
    mockInitiateResponse,
    mockSimulationResponse,
    mockSendApplicationResponse, mockStatusResponse
} from "@/api/model/Mocks";

const basePath = "/pensjon/selvbetjening/inntektsplanleggeren";

const MOCKS_ENABLED = true;

const headers = {
    'Content-Type': 'application/json'
}

export async function getState(): Promise<FormState> {
    const res = await fetch(basePath + `/persistence/inntekter`, {
        method: "GET",
        credentials: "include",
        headers: headers,
    });

    // if (MOCKS_ENABLED) {
    //     return {
    //         brukerInntekter: {
    //             arbeidsinntekt: 0,
    //             pensjon: 0,
    //             trygdeytelser: 0,
    //             andreInntekter: 0,
    //             andreYtelser: 0
    //         },
    //         epsInntekter: {
    //             arbeidsinntekt: 0,
    //             pensjon: 0,
    //             trygdeytelser: 0,
    //             andreInntekter: 0,
    //             andreYtelser: 0
    //         }
    //     }
    // }

    if (!res.ok) {
        throw new Error("Fikk ikke 2xx respons fra server");
    }

    return res.json();
}

export async function saveState(state: FormState): Promise<void> {
    const res = await fetch(basePath + `/persistence/inntekter`, {
        method: "POST",
        credentials: "include",
        headers: headers,
        body: JSON.stringify(state)
    });

    if (!res.ok) {
        throw new Error("Fikk ikke 2xx respons fra server");
    }
}

export async function deleteState(): Promise<void> {
    const res = await fetch(basePath + '/persistence/inntekter', {
        method: "DELETE",
        credentials: "include",
        headers: headers,
    });

    if (!res.ok) {
        throw new Error("Fikk ikke 2xx respons fra server");
    }
}

export async function getInitiate(): Promise<GetInntektsgrenseResponse> {
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
