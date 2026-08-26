import { lagHeadere, lagResponse, type Result } from '@/api/apiHjelpere'
import type {
    InntekterResponse,
    PersonInntekter,
    SendApplicationResponse,
    SimulationResponse,
    StatusResponse,
    SubmitInntekterRequest,
} from '@/api/model/ApiRequests'
import { BASE_PATH } from '@/routes'

export async function getInntekterForSimulering(year: number): Promise<Result<InntekterResponse>> {
    const searchParams = new URLSearchParams(document.location.search)
    const pid: string | null = searchParams.get('pid')

    const headers = lagHeadere(pid)

    return await fetch(`${BASE_PATH}/api/inntekter?simuleringsaar=${year}`, {
        method: 'GET',
        credentials: 'include',
        headers: headers,
    }).then(async (response) => {
        return lagResponse(response)
    })
}

export async function getInntekter(aar: number): Promise<Result<InntekterResponse>> {
    const searchParams = new URLSearchParams(document.location.search)
    const pid: string | null = searchParams.get('pid')

    const headers = lagHeadere(pid)

    return await fetch(`${BASE_PATH}/api/inntekter-for-aar?aar=${aar}`, {
        method: 'GET',
        credentials: 'include',
        headers: headers,
    }).then(async (response) => {
        return lagResponse(response)
    })
}

export async function simulate(brukerInntekter: PersonInntekter, epsInntekter: PersonInntekter | null, year: number): Promise<Result<SimulationResponse>> {
    const searchParams = new URLSearchParams(document.location.search)
    const pid: string | null = searchParams.get('pid')

    const headers = lagHeadere(pid)

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
        return lagResponse(response)
    })
}

export async function send(brukerInntekter: PersonInntekter, epsInntekter: PersonInntekter | null, year: number): Promise<Result<SendApplicationResponse>> {
    const searchParams = new URLSearchParams(document.location.search)
    const pid: string | null = searchParams.get('pid')

    const headers = lagHeadere(pid)

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
        return lagResponse(response)
    })
}

export async function getStatus(valgtaar: number, innsendingstidspunkt: string): Promise<Result<StatusResponse>> {
    const searchParams = new URLSearchParams(document.location.search)
    const pid: string | null = searchParams.get('pid')

    const headers = lagHeadere(pid)

    const url = encodeURI(`${BASE_PATH}/api/status?valgtaar=${valgtaar}&innsendingstidspunkt=${innsendingstidspunkt}`)
    return await fetch(url, {
        method: 'GET',
        credentials: 'include',
        headers: headers,
    }).then(async (response) => {
        return lagResponse(response)
    })
}
