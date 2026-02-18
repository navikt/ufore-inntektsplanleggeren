export const getPidQueryParamString = (href?: string) => {
    const searchParams = new URLSearchParams(document.location.search)
    const pid = searchParams.get('pid')

    if (pid === null) {
        return href ? `#${href}` : ''
    }

    return '?pid=' + pid + (href ? `#${href}` : '')
}
