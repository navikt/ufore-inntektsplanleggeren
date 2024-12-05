


import {Alert, VStack} from "@navikt/ds-react";
import React from "react";


export function Error() {
    return (
        <VStack>
                {/*TODO proper error message*/}
                <Alert variant="error">
                    Noe gikk feil
                </Alert>
        </VStack>
    )
}