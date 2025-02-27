import { BodyShort, Box, Loader, VStack } from '@navikt/ds-react'
import React from 'react'

export function LoadingBox() {
  return (
    <Box background="bg-subtle" padding="16" borderRadius="large">
      <VStack align="center" gap="20">
        <Loader size="3xlarge" />
        <BodyShort align="center">Vent mens vi laster inn siden.</BodyShort>
      </VStack>
    </Box>
  )
}
