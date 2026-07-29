import { BASE_PATH } from '@/routes'

export interface VeilederBannerInfo {
    pid: string
    borgerNavn?: string
    veilederNavn?: string
}

export async function hentVeilederBannerInfo(): Promise<VeilederBannerInfo> {
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

    return await fetch(`${BASE_PATH}/api/veilederbanner`, {
        method: 'GET',
        credentials: 'include',
        headers: headers,
    }).then(async (response) => {
        if (response.status >= 300) {
            throw Error()
        }
        return response.json()
    })
}
