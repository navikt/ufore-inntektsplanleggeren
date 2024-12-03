import {BodyShort, Box, Loader, VStack} from "@navikt/ds-react";
import React from "react";

export function LoadingBox() {
    return (
        <Box background="bg-subtle" padding="16">
            <VStack className="form-container" align="center" gap="20">
                <Loader size="3xlarge"/>
                <VStack>
                    <BodyShort>Vent mens vi laster inn siden.</BodyShort>
                </VStack>
            </VStack>
        </Box>
    );
}