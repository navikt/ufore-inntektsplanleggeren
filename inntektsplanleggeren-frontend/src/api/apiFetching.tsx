import type {
    InntekterResponse,
    PersonInntekter,
    SendApplicationResponse,
    SimulationResponse,
    StatusResponse,
    SubmitInntekterRequest,
} from '@/api/model/ApiRequests'
import { ErrorResponse } from '@/components/common/Error'

export const BASE_PATH = '/uforetrygd/selvbetjening/inntektsplanleggeren'

export async function getInntekterForSimulering(year: number): Promise<InntekterResponse | ErrorResponse> {
    const searchParams = new URLSearchParams(document.location.search)
    const pid: string | null = searchParams.get('pid')

    let headers: HeadersInit

    if (pid) {
        headers = {
            'Content-Type': 'application/json',
            pid: pid,
        }
    } else {
        headers = {
            'Content-Type': 'application/json',
        }
    }

    return await fetch(`${BASE_PATH}/api/inntekter?simuleringsaar=${year}`, {
        method: 'GET',
        credentials: 'include',
        headers: headers,
    }).then(async (response) => {
        if (response.status === 403) {
            return await response.json().then((it) => new ErrorResponse(it))
        }
        if (response.status >= 300) {
            throw Error()
        }

        return response.json()
    })
}

export async function getInntekter(aar: number): Promise<InntekterResponse | ErrorResponse> {
    const searchParams = new URLSearchParams(document.location.search)
    const pid: string | null = searchParams.get('pid')

    let headers: HeadersInit

    if (pid) {
        headers = {
            'Content-Type': 'application/json',
            pid: pid,
        }
    } else {
        headers = {
            'Content-Type': 'application/json',
        }
    }

    return await fetch(`${BASE_PATH}/api/inntekter-for-aar?aar=${aar}`, {
        method: 'GET',
        credentials: 'include',
        headers: headers,
    }).then(async (response) => {
        if (response.status === 403) {
            return await response.json().then((it) => new ErrorResponse(it))
        }
        if (response.status >= 300) {
            throw Error()
        }

        return response.json()
    })
}

export async function simulate(
    brukerInntekter: PersonInntekter,
    epsInntekter: PersonInntekter | null,
    year: number
): Promise<SimulationResponse | ErrorResponse> {
    const searchParams = new URLSearchParams(document.location.search)
    const pid: string | null = searchParams.get('pid')

    let headers: HeadersInit

    if (pid) {
        headers = {
            'Content-Type': 'application/json',
            pid: pid,
        }
    } else {
        headers = {
            'Content-Type': 'application/json',
        }
    }

    const request: SubmitInntekterRequest = {
        bruker: brukerInntekter,
        eps: epsInntekter,
    }

    return await fetch(`${BASE_PATH}/api/simuler?simuleringsaar=${year}`, {
        method: 'POST',
        credentials: 'include',
        headers: headers,
        body: JSON.stringify(request),
    }).then(async (response) => {
        if (response.status === 403) {
            return await response.json().then((it) => new ErrorResponse(it))
        }
        if (response.status >= 300) {
            throw Error()
        }

        return response.json()
    })
}

export async function send(
    brukerInntekter: PersonInntekter,
    epsInntekter: PersonInntekter | null,
    year: number
): Promise<SendApplicationResponse | ErrorResponse> {
    const searchParams = new URLSearchParams(document.location.search)
    const pid: string | null = searchParams.get('pid')

    let headers: HeadersInit

    if (pid) {
        headers = {
            'Content-Type': 'application/json',
            pid: pid,
        }
    } else {
        headers = {
            'Content-Type': 'application/json',
        }
    }

    const request: SubmitInntekterRequest = {
        bruker: brukerInntekter,
        eps: epsInntekter,
    }

    return await fetch(`${BASE_PATH}/api/send?simuleringsaar=${year}`, {
        method: 'POST',
        credentials: 'include',
        headers: headers,
        body: JSON.stringify(request),
    }).then(async (response) => {
        if (response.status === 403) {
            return await response.json().then((it) => new ErrorResponse(it))
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

    let headers: HeadersInit

    if (pid) {
        headers = {
            'Content-Type': 'application/json',
            pid: pid,
        }
    } else {
        headers = {
            'Content-Type': 'application/json',
        }
    }

    const url = encodeURI(`${BASE_PATH}/api/status?valgtaar=${valgtaar}&innsendingstidspunkt=${innsendingstidspunkt}`)
    return await fetch(url, {
        method: 'GET',
        credentials: 'include',
        headers: headers,
    }).then(async (response) => {
        if (response.status === 403) {
            return await response.json().then((it) => new ErrorResponse(it))
        }
        if (response.status >= 300) {
            throw Error()
        }

        return response.json()
    })
}
