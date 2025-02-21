export const getPidQueryParamString = () => {
    const searchParams = new URLSearchParams(document.location.search)
    const pid = searchParams.get('pid')
    if (pid === null) {
        return ''
    }
    return '?pid=' + pid
}