import './veilederbanner.css'
import { BodyShort, Box, CopyButton, HStack, InternalHeader, Spacer } from '@navikt/ds-react'
import { useContext } from 'react'
import { getPidQueryParamString } from '@/components/utils/UrlUtil'
import { DataContext } from '@/context/DataContextProvider'
import { BASE_PATH } from '@/routes'

export default function VeilederBanner() {
    const { borgerInfo, loggetInnSom } = useContext(DataContext)

    const formatFnr = (fnr: string) => {
        return `${fnr.slice(0, 6)} ${fnr.slice(6)}`
    }

    return (
        <>
            <InternalHeader>
                <InternalHeader.Title href={BASE_PATH + getPidQueryParamString()}>Inntektsplanleggeren</InternalHeader.Title>
                <Spacer />
                {loggetInnSom && <InternalHeader.User name={loggetInnSom} />}
            </InternalHeader>
            {borgerInfo && (
                <Box borderWidth="0 0 1 0" borderColor="neutral-subtle">
                    <HStack align="center" gap="space-8" className="borger-informasjon">
                        <BodyShort size="small" weight="semibold">
                            {borgerInfo.navn || ''}
                        </BodyShort>
                        <span aria-hidden="true">/</span>
                        <HStack align="center" gap="space-4">
                            <BodyShort data-testid="borger-fnr" size="small" weight="semibold">
                                {formatFnr(borgerInfo.pid)}
                            </BodyShort>
                            <CopyButton size="small" copyText={borgerInfo.pid || ''} />
                        </HStack>
                        <Spacer />
                    </HStack>
                </Box>
            )}
        </>
    )
}
