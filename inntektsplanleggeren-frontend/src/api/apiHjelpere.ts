import { ApiError, ErrorCode } from '@/components/common/Error'

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

export const håndterReponse = async (response: Response) => {
    if (!response.ok) {
        const error = await response.json()
        const errorCode = (error.detail ?? error.message ?? ErrorCode.GENERIC_ERROR) as ErrorCode
        throw new ApiError(errorCode)
    }
    return response.json()
}
