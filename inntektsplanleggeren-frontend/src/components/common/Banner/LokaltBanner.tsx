import type { PropsWithChildren } from 'react'
import BorgerBanner from '@/components/common/Banner/BorgerBanner'
import VeilederBanner from '@/components/common/Banner/VeilederBanner'

// LokaltBanner brukes kun når vi kjører opp lokalt for å simulere header i veileder vs borger-modus
// fordi det anbefales å ikke bruke nav-dekoratoren-moduler i client side rendering
// (https://github.com/navikt/nav-dekoratoren-moduler?tab=readme-ov-file#client-side-rendering)
/* eslint-disable  @typescript-eslint/no-explicit-any */
export default function LokaltBanner({ children }: PropsWithChildren) {
    const mode = import.meta.env.VITE_MODE
    const erVeileder = mode.includes('veileder')

    return (
        <>
            {erVeileder ? (
                <>
                    <VeilederBanner />
                    {children}
                </>
            ) : (
                <BorgerBanner>{children}</BorgerBanner>
            )}
        </>
    )
}
