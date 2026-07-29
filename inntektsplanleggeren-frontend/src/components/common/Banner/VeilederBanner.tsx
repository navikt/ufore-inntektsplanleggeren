import './veilederbanner.css'
import { BodyShort, Box, CopyButton, HStack, InternalHeader, Spacer } from '@navikt/ds-react'
import { useEffect, useState } from 'react'
import { hentVeilederBannerInfo, type VeilederBannerInfo } from '@/api/hentVeilederBannerInfo'
import { getPidQueryParamString } from '@/components/utils/UrlUtil'
import { BASE_PATH } from '@/routes'

export default function VeilederBanner() {
    const [veilederBannerInfo, setVeilederBannerInfo] = useState<VeilederBannerInfo>()

    useEffect(() => {
        const hentInfo = async () => {
            const veileder = await hentVeilederBannerInfo()
            setVeilederBannerInfo(veileder)
        }
        hentInfo()
    }, [])

    const formatFnr = (fnr: string) => {
        return `${fnr.slice(0, 6)} ${fnr.slice(6)}`
    }

    return (
        <>
            <InternalHeader>
                <InternalHeader.Title href={BASE_PATH + getPidQueryParamString()}>Inntektsplanleggeren</InternalHeader.Title>
                <Spacer />
                {veilederBannerInfo?.veilederNavn && <InternalHeader.User name={veilederBannerInfo.veilederNavn} />}
            </InternalHeader>
            {veilederBannerInfo?.pid && (
                <Box borderWidth="0 0 1 0" borderColor="neutral-subtle">
                    <HStack align="center" gap="space-8" className="borger-informasjon">
                        <BodyShort size="small" weight="semibold">
                            {veilederBannerInfo.borgerNavn || ''}
                        </BodyShort>
                        <span aria-hidden="true">/</span>
                        <HStack align="center" gap="space-4">
                            <BodyShort data-testid="borger-fnr" size="small" weight="semibold">
                                {formatFnr(veilederBannerInfo.pid)}
                            </BodyShort>
                            <CopyButton size="small" copyText={veilederBannerInfo.pid || ''} />
                        </HStack>
                        <Spacer />
                    </HStack>
                </Box>
            )}
        </>
    )
}
