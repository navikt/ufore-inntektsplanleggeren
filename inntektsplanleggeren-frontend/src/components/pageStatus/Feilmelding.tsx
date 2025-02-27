import { Alert } from '@navikt/ds-react'
import { useContext } from 'react'
import { DataContext } from '@/DataContextProvider'

export function Feilmelding() {
  const { feilmeldingkode } = useContext(DataContext)

  return (
    <>
      <div id="error-div">{getCorrectFeilmelding(feilmeldingkode)}</div>
    </>
  )
}

function getCorrectFeilmelding(param: string) {
  switch (param) {
    case 'OVERLAPP_I_SAMBOERPERIODER':
      return (
        <Alert variant="error">
          {' '}
          Du har forsøkt å lagre et samboerforhold som overlapper med et tidligere samboerforhold. Dette er ikke mulig.
          Om du mener dette er feil, ta kontakt med oss i Nav.{' '}
        </Alert>
      )
    case 'SAMBOERPERIODE_ALLEREDE_AVSLUTTET':
      return <Alert variant="error"> Kan ikke avslutte en samboerperiode som allerede er avsluttet. </Alert>
    case 'SLUTT_DATO_FREM_I_TID':
      return <Alert variant="error"> Du kan ikke registrere et samboerforhold med sluttdato i fremtiden. </Alert>
    case 'SAMBOER_MED_SEG_SELV':
      return <Alert variant="error"> Du kan ikke registrere et samboerforhold med deg selv. </Alert>
    case 'SLUTT_DATO_FOER_START_DATO':
      return <Alert variant="error"> Et samboerforhold kan ikke avsluttes før det starter. </Alert>
    case 'KAN_IKKE_REAKTIVERE_AVSLUTTET_SAMBOERPERIODE':
      return <Alert variant="error"> Det er ikke mulig å gjenåpne et avsluttet samboerforhold. </Alert>
    default:
      return (
        <Alert variant="error"> Det skjedde en feil under lagring av samboerforhold. Vennligst kontakt oss. </Alert>
      )
  }
}
