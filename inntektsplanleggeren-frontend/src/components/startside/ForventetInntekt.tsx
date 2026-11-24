import { BodyLong, Box, Heading, VStack } from '@navikt/ds-react'
import type { UføretrygdOversikt } from '@/api/hentStartsideData'
import { FormatKroner } from '@/components/utils/FormatKroner'

interface Props {
    data: (UføretrygdOversikt | null)[]
}

export default function ForventetInntekt({ data }: Props) {
    return (
        <VStack gap="space-32">
            {data
                .filter((oversikt) => oversikt !== null)
                .map((oversikt) => (
                    <Box key={oversikt.forAar} borderRadius="12" padding="space-16" background="accent-soft">
                        <VStack gap="space-28">
                            <Heading level="2" size="small">
                                Registrert forventet inntekt for {oversikt.forAar}
                            </Heading>
                            <BodyLong>
                                Din registrerte forventede inntekt:{' '}
                                <strong>
                                    <FormatKroner value={oversikt.forventetInntekt} />
                                </strong>
                            </BodyLong>
                            {oversikt.harBarnetilleggFellesbarn && (
                                <BodyLong>
                                    Annen forelder du bor med sin forventede inntekt:{' '}
                                    <strong>
                                        <FormatKroner value={oversikt.forventetInntektAnnenForelder} />
                                    </strong>
                                </BodyLong>
                            )}
                            <BodyLong>
                                Ditt bunnfradrag:{' '}
                                <strong>
                                    <FormatKroner value={oversikt.bunnfradrag} />
                                </strong>
                            </BodyLong>
                            <BodyLong>
                                Reduksjonsprosent: <strong>{oversikt.reduksjonsprosent} prosent</strong>
                            </BodyLong>
                            <BodyLong>
                                Inntektstak:{' '}
                                <strong>
                                    <FormatKroner value={oversikt.inntektstak} />
                                </strong>
                            </BodyLong>
                        </VStack>
                    </Box>
                ))}
        </VStack>
    )
}
