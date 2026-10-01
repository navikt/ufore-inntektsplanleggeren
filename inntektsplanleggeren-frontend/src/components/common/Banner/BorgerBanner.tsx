import { injectDecoratorClientSide } from '@navikt/nav-dekoratoren-moduler'
import { type PropsWithChildren, useEffect, useState } from 'react'
import RepresentasjonBanner from '@/components/common/Banner/RepresentasjonBanner'

export default function BorgerBanner({ children }: PropsWithChildren) {
    const mode = import.meta.env.VITE_MODE
    const erVeileder = mode.includes('veileder')
    const decoratorEnv = import.meta.env.VITE_DECORATOR_ENV
    const [Decorator, setDecorator] = useState<any>(null)

    useEffect(() => {
        if (!erVeileder) {
            injectDecoratorClientSide({
                env: decoratorEnv,
                params: {
                    teamName: 'ufore',
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
            {Decorator && (
                <>
                    {Decorator.Header && <Decorator.Header />}
                    <RepresentasjonBanner />
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
    )
}
