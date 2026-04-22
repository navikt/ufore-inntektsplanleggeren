import { injectDecoratorClientSide } from '@navikt/nav-dekoratoren-moduler'

import VeilederBanner from '@/components/common/Banner/VeilederBanner'
import { PropsWithChildren, useEffect, useState } from 'react'
import BorgerBanner from '@/components/common/Banner/BorgerBanner'


// LokaltBanner brukes kun når vi kjører opp lokalt for å simulere header i veileder vs borger-modus
// fordi det anbefales å ikke bruke nav-dekoratoren-moduler i client side rendering
// (https://github.com/navikt/nav-dekoratoren-moduler?tab=readme-ov-file#client-side-rendering)
/* eslint-disable  @typescript-eslint/no-explicit-any */
export default function LokaltBanner({ children }: PropsWithChildren) {
    const mode = import.meta.env.VITE_MODE
    const erVeileder = mode.includes('veileder')
    const [Decorator, setDecorator] = useState<any>(null)

    useEffect(() => {
        if (!erVeileder) {
            injectDecoratorClientSide({
                env: 'dev',
                params: {
                    context: 'privatperson',
                    breadcrumbs: [
                        {
                            title: 'Min side',
                            url: 'https://www.nav.no/minside',
                        },
                        {
                            title: 'Din uføretrygd',
                            url: 'https://www.nav.no/uføretrygd',
                        },
                    ],
                },
            }).then((d) => {
                setDecorator(d)
            })
        }
    }, [erVeileder])

    return (
        <>
            {erVeileder ? (
                <>
                    <VeilederBanner />
                    {children}
                </>
            ) : (
                <>
                    {Decorator && (
                        <>
                            {Decorator.Header && <Decorator.Header />}
                            <BorgerBanner />
                        </>
                    )}
                    {children}
                    {Decorator && (
                        <>
                            {Decorator.Footer && <Decorator.Footer />}
                            {Decorator.Scripts && <Decorator.Scripts />}
                            <script src="https://widget.uxsignals.com/embed.js" async></script>
                        </>
                    )}
                </>
            )}
        </>
    )
}
