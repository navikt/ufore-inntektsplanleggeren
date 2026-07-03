import '@navikt/ds-tokens'
import '@navikt/ds-css'
import { BodyShort, Heading, VStack } from '@navikt/ds-react'
import './nedetidpage.css'
import { InformationSquareIcon } from '@navikt/aksel-icons'

export const NedetidPage = () => {
    return (
        <main className="nedetid-main">
            <Heading size="xlarge" level="1" className="nedetid-header">
                Inntektsplanleggeren
            </Heading>
            <VStack className="nedetid-container">
                <InformationSquareIcon aria-hidden width={80} height={80} color={'#555D6A'} />
                <Heading size="medium" align="center" style={{ margin: '1.5rem 0 1rem 0' }}>
                    Inntektsplanleggeren er midlertidig stengt
                </Heading>
                <BodyShort align="center">
                    Inntektsplanleggeren er ikke tilgjengelig til fredag 26. juni kl. 9:00 på grunn av vedlikehold. Vi beklager for ulempen.
                </BodyShort>
            </VStack>
        </main>
    )
}
