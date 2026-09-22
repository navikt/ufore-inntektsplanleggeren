// Midlertidig testpanel for å verifisere @nais/apm-instrumentering lokalt.
// Se console i devtools mens du trykker på knappene — se svar fra Copilot for
// hva som forventes å skje for hver knapp. FJERN DENNE FILEN når testingen er ferdig.
import { captureException, captureMessage } from '@nais/apm'
import { useState } from 'react'

function ThrowOnRender(): React.ReactElement {
    throw new Error('[apm-test] Render-feil — skal fanges av ApmErrorBoundary')
}

export function ApmDebugPanel() {
    const [triggerRenderError, setTriggerRenderError] = useState(false)

    if (triggerRenderError) {
        return <ThrowOnRender />
    }

    return (
        <div
            style={{
                position: 'fixed',
                bottom: 8,
                right: 8,
                zIndex: 9999,
                display: 'flex',
                flexDirection: 'column',
                gap: 4,
                padding: 8,
                background: '#fff',
                border: '1px solid #000',
            }}
        >
            <strong>APM debug</strong>

            <button type="button" onClick={() => setTriggerRenderError(true)}>
                1. Render-feil (ApmErrorBoundary)
            </button>

            <button
                type="button"
                onClick={() => {
                    throw new Error('[apm-test] Uncaught feil i onClick — fanges automatisk, ikke av boundary')
                }}
            >
                2. Uncaught feil i event handler
            </button>

            <button
                type="button"
                onClick={() => {
                    Promise.reject(new Error('[apm-test] Unhandled promise rejection'))
                }}
            >
                3. Unhandled promise rejection
            </button>

            <button
                type="button"
                onClick={() => {
                    try {
                        throw new Error('[apm-test] Fanget i try/catch')
                    } catch (e) {
                        captureException(e, { context: { kilde: 'ApmDebugPanel' } })
                    }
                }}
            >
                4. Manuell captureException
            </button>

            <button type="button" onClick={() => captureMessage('[apm-test] manuell captureMessage', 'warning')}>
                5. Manuell captureMessage
            </button>
        </div>
    )
}
