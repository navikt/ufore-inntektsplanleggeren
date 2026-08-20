import { BASE_PATH } from '@/api/apiFetching'
import { håndterReponse, lagHeadere } from '@/api/apiHjelpere'
import type { Message } from '@/api/model/ApiRequests'

export async function hentStartsideData(): Promise<StartsideData> {
    const searchParams = new URLSearchParams(document.location.search)
    const pid: string | null = searchParams.get('pid')

    const headers = lagHeadere(pid)

    return await fetch(`${BASE_PATH}/api/startside`, {
        method: 'GET',
        credentials: 'include',
        headers: headers,
    }).then(async (response) => {
        return håndterReponse(response)
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
