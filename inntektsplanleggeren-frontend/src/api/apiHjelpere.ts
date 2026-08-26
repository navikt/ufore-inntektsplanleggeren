import type { ErrorCode } from '@/components/common/Error'

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

export type Result<T, E = ErrorCode> = { ok: true; data: T } | { ok: false; error: E }
