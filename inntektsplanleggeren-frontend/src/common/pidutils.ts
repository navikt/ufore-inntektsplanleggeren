export function pidFormat(pid: string) {
    return pid.slice(0, 6) + ' ' + pid.slice(6, 11)
}

export function pidFormatAdjustableView(pid: string, isDesktop: boolean) {
    if (isDesktop) {
        return pidFormat(pid)
    }
    return pid.replace('*****', '')
}
