import { ErrorCode } from '@/components/common/Error'

export const lagHeadere = (pid: string | null): HeadersInit => {
    return pid
        ? {
              'Content-Type': 'application/json',
              pid: pid,
          }
        : {
              'Content-Type': 'application/json',
          }
}

export const lagResponse = async <T>(response: Response): Promise<Result<T>> => {
    if (!response.ok) {
        const error = await response.json()
        const errorCode = (error.detail ?? error.message ?? ErrorCode.GENERIC_ERROR) as ErrorCode
        return { ok: false, error: errorCode }
    }

    const data = await response.json()

    return { ok: true, data: data }
}

export type Result<T, E = ErrorCode> = { ok: true; data: T } | { ok: false; error: E }
