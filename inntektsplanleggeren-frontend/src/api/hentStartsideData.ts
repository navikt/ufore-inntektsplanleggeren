import { hentData, lagHeadere, type Result } from '@/api/apiHjelpere'
import type { Message } from '@/api/model/ApiRequests'
import { BASE_PATH } from '@/routes'

export async function hentStartsideData(): Promise<Result<StartsideData>> {
    const searchParams = new URLSearchParams(document.location.search)
    const pid: string | null = searchParams.get('pid')

    const headers = lagHeadere(pid)

    return hentData(`${BASE_PATH}/api/startside`, 'GET', headers)
}

export interface StartsideData {
    messages: Message[]
    uforetrygdIAar: UføretrygdOversikt | null
    uforetrygdNesteAar: UføretrygdOversikt | null
    aarKanRegistrereInntekt: number[]
    seTallForAar: number | null
}

export interface UføretrygdOversikt {
    forAar: number
    forventetInntekt: number
    forventetInntektAnnenForelder: number
    bunnfradrag: number
    reduksjonsprosent: number
    inntektstak: number
    harBarnetilleggFellesbarn: boolean
}
