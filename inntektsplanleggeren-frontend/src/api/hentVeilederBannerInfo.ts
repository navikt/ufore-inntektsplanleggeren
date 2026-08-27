import { hentData, lagHeadere, type Result } from '@/api/apiHjelpere'
import { BASE_PATH } from '@/routes'

export interface VeilederBannerInfo {
    pid: string
    borgerNavn?: string
    veilederNavn?: string
}

export async function hentVeilederBannerInfo(): Promise<Result<VeilederBannerInfo>> {
    const searchParams = new URLSearchParams(document.location.search)
    const pid: string | null = searchParams.get('pid')

    const headers = lagHeadere(pid)

    return hentData(`${BASE_PATH}/api/veilederbanner`, 'GET', headers)
}
