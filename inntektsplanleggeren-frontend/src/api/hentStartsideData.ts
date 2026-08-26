import { BASE_PATH } from '@/api/apiFetching'
import { lagHeadere, type Result } from '@/api/apiHjelpere'
import type { Message } from '@/api/model/ApiRequests'
import { ErrorCode } from '@/components/common/Error'

export async function hentStartsideData(): Promise<Result<StartsideData>> {
    const searchParams = new URLSearchParams(document.location.search)
    const pid: string | null = searchParams.get('pid')

    const headers = lagHeadere(pid)

    return await fetch(`${BASE_PATH}/api/startside`, {
        method: 'GET',
        credentials: 'include',
        headers: headers,
    }).then(async (response) => {
        if (!response.ok) {
            const error = await response.json()
            const errorCode = (error.detail ?? error.message ?? ErrorCode.GENERIC_ERROR) as ErrorCode
            return { ok: false, error: errorCode }
        }

        const data = await response.json()

        return { ok: true, data: data }
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
