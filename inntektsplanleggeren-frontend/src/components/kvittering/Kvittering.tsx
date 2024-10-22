import {Heading, VStack, Alert, BodyLong} from "@navikt/ds-react";
import React from "react";

export const Kvittering = () => {
    return (
        <VStack className="form-container">
            <Heading level="2" size="small">Kvittering</Heading>
            <Alert variant="success">
                <Heading spacing size="small" level="3">        Viktig informasjon      </Heading>
                Søknaden din hart blitt sendt
                {/*<BodyLong>Something</BodyLong>*/}
                {/*<BodyLong>Something</BodyLong>*/}
            </Alert>

            <Heading size={"medium"}>Husk å oppdatere skattekortet</Heading>
            <BodyLong>
                Hvis du har fått endret inntekt kan det være at skattekortet ditt må oppdateres.
                Les om skattekort og endre det hos Skatteetaten (åpnes i ny fane).
            </BodyLong>

            <Heading size={"medium"}>Hvis du tjener mer eller mindre enn du tror nå</Heading>
            <BodyLong>
                Hvis du senere ser at inntekten din kommer til å bli blir høyere eller lavere enn det du meldte inn i inntektsplanleggeren, bør du sende inn en endring.
                Du sender inn opplysninger om ny forventet inntekt gjennom inntektsplanleggeren.
            </BodyLong>

            <Heading size={"medium"}>Etteroppgjør</Heading>
            <BodyLong>
                På høsten får vi inntektsopplysninger fra Skatteetaten for året før. Da ser vi hvor mye du faktisk hadde i inntekt opp mot den forventede inntekten og hvor
                mye uføretrygden eventuelt ble redusert. Dersom du har fått for mye eller for lite utbetalt uføretrygd det året, vil du få en etterbetaling eller et krav om tilbakebetaling.
                Les om etteroppgjøret (åpnes i ny fane).
            </BodyLong>
        </VStack>
    );
};