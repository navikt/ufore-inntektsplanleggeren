import {
    PersonInntekter, GetInntektsgrenseResponse,
    InntekterResponse, SubmitInntekterRequest, SimulationResponse, SendApplicationResponse, StatusResponse
} from "@/api/model/ApiRequests";
import {ErrorResponse} from "@/components/common/Error";

 const isMock = process.env.isMock || false
 const PORT = process.env.MOCK_PORT || "8080"
 const BASE_URL = isMock ? "http://" + window.location.hostname + ":" + PORT + import.meta.env.BASE_URL + "/"
     : import.meta.env.BASE_URL + "/"

export async function getInitiate(): Promise<GetInntektsgrenseResponse | ErrorResponse> {
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

    return await fetch(BASE_URL + `api/initiate`, {
        method: "GET",
        credentials: "include",
        headers: headers
    }).then(async response => {
        if (response.status === 403) {
            return await response.json().then(it => new ErrorResponse(it))
        }
        if (response.status >= 300) {
            throw Error()
        }

        return response.json()
    })
}



export async function getInntekter(year: number): Promise<InntekterResponse | ErrorResponse> {
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

    return await fetch(BASE_URL + `api/inntekter?simuleringsaar=${year}`, {
        method: "GET",
        credentials: "include",
        headers: headers
    }).then(async response => {
        if (response.status === 403) {
            return await response.json().then(it => new ErrorResponse(it))
        }
        if (response.status >= 300) {
            throw Error()
        }

        return response.json()
    })
}

export async function simulate(brukerInntekter: PersonInntekter, epsInntekter: PersonInntekter | null, year: number): Promise<SimulationResponse | ErrorResponse> {
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

    return await fetch(BASE_URL + `api/simuler?simuleringsaar=${year}`, {
        method: "POST",
        credentials: "include",
        headers: headers,
        body: JSON.stringify(request)
    }).then(async response => {
        if (response.status === 403) {
            return await response.json().then(it => new ErrorResponse(it))
        }
        if (response.status >= 300) {
            throw Error()
        }

        return response.json()
    })
}

export async function send(brukerInntekter: PersonInntekter, epsInntekter: PersonInntekter | null, year: number): Promise<SendApplicationResponse | ErrorResponse> {
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

    return await fetch(BASE_URL + `api/send?simuleringsaar=${year}`, {
        method: "POST",
        credentials: "include",
        headers: headers,
        body: JSON.stringify(request)
    }).then(async response => {
        if (response.status === 403) {
            return await response.json().then(it => new ErrorResponse(it))
        }
        if (response.status >= 300) {
            throw Error()
        }

        return response.json()
    })
}

export async function getStatus(valgtaar: number, innsendingstidspunkt: string): Promise<StatusResponse | ErrorResponse> {
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

    const url = encodeURI(BASE_URL + `api/status?valgtaar=${valgtaar}&innsendingstidspunkt=${innsendingstidspunkt}`)
    return await fetch(url, {
        method: "GET",
        credentials: "include",
        headers: headers,
    }).then(async response => {
        if (response.status === 403) {
            return await response.json().then(it => new ErrorResponse(it))
        }
        if (response.status >= 300) {
            throw Error()
        }

        return response.json()
    })
}
