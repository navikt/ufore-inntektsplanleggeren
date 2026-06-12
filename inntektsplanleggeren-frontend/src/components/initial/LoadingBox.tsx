import { BodyShort, Box, Loader, VStack } from '@navikt/ds-react'

export function LoadingBox() {
    return (
        <Box background="neutral-soft" padding="space-64" borderRadius="8">
            <VStack align="center" gap="space-80">
                <Loader size="3xlarge" />
                <BodyShort align="center">Vent mens vi laster inn siden.</BodyShort>
            </VStack>
        </Box>
    )
}
