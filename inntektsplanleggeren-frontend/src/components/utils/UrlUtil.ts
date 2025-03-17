export const getPidQueryParamString = () => {
  const searchParams = new URLSearchParams(document.location.search)
  const pid = searchParams.get('pid')
  if (pid === null) {
    return ''
  }
  return '?pid=' + pid
}

export function getUrlUforeInnboks(){
  const searchParams = new URLSearchParams(document.location.search)
  const pid = searchParams.get('pid')
  const url = import.meta.env.VITE_NAV_UFORE_INNBOKS_URL
  return url.replace("PID", pid)
}

