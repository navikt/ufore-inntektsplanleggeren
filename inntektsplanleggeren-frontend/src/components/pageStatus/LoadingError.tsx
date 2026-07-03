import { Alert } from '@navikt/ds-react'
import './Error.css'

// TODO: Brukes disse? Gjelder også i DataContextProvider
export function LoadingError() {
    return (
        <>
            <div id="error-div">
                <Alert variant="error">Noe gikk galt under innhenting av opplysninger om familieforhold. Prøv igjen senere.</Alert>
            </div>
        </>
    )
}
