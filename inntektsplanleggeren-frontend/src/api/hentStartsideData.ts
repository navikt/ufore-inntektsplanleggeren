import { BASE_PATH } from '@/api/apiFetching'
import type { Message } from '@/api/model/ApiRequests'
import { ErrorResponse } from '@/components/common/Error'

export async function hentStartsideData(): Promise<StartsideData | ErrorResponse> {
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

    return await fetch(`${BASE_PATH}/api/initiate`, {
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

export interface StartsideData {
    messages: Message[]
    uforetrygd: UføretrygdOversikt
}

export interface UføretrygdOversikt {
    forventetInntekt: Record<number, number>
    forventetInntektAnnenForelder: Record<number, number | null>
    inntektsgrense: number
    reduksjonsprosent: number
    inntektstak: number
    aarKanRegistrereInntekt: number[]
    seTallForAar: number | null
    harBarnetilleggFellesbarn: boolean
}
