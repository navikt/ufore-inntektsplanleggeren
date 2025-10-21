import './veilederbanner.css'
import { BodyShort, Box, CopyButton, HStack, InternalHeader, Spacer } from '@navikt/ds-react'
import { useContext } from 'react'
import { DataContext } from '@/context/DataContextProvider'
import { getFullPathForPage, PageLinks } from '@/FormContainer'

export default function VeilederBanner() {
    const { borgerInfo, loggetInnSom } = useContext(DataContext)

    const formatFnr = (fnr: string) => {
        return `${fnr.slice(0, 6)} ${fnr.slice(6)}`
    }

    return (
        <>
            <InternalHeader>
                <InternalHeader.Title href={getFullPathForPage(PageLinks.INDEX)}>Inntektsplanleggeren</InternalHeader.Title>
                <Spacer />
                {loggetInnSom && <InternalHeader.User name={loggetInnSom} />}
            </InternalHeader>
            {borgerInfo && (
                <Box borderWidth="0 0 1 0" borderColor="border-divider">
                    <HStack align="center" gap="2" className="borger-informasjon">
                        <BodyShort size="small" weight="semibold">
                            {borgerInfo && borgerInfo.navn}
                        </BodyShort>
                        <span aria-hidden="true">/</span>
                        <HStack align="center" gap="1">
                            <BodyShort data-testid="borger-fnr" size="small" weight="semibold">
                                {borgerInfo?.pid && formatFnr(borgerInfo.pid)}
                            </BodyShort>
                            <CopyButton size="small" copyText={borgerInfo?.pid || ''} />
                        </HStack>
                        <Spacer />
                    </HStack>
                </Box>
            )}
        </>
    )
}
