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

export const hentData = async <T>(url: string, method: Method, headers: HeadersInit, body?: string): Promise<Result<T>> => {
    try {
        const response = await fetch(url, {
            method: method,
            credentials: 'include',
            headers: headers,
            body: body,
        })

        if (!response.ok) {
            const error = await response.json()
            const errorCode = (error.detail ?? error.message ?? ErrorCode.GENERIC_ERROR) as ErrorCode
            return { ok: false, error: errorCode }
        }

        const data = await response.json()

        return { ok: true, data: data }
    } catch {
        return { ok: false, error: ErrorCode.GENERIC_ERROR }
    }
}

export type Result<T, E = ErrorCode> = { ok: true; data: T } | { ok: false; error: E }

type Method = 'GET' | 'POST' | 'PUT' | 'DELETE'
