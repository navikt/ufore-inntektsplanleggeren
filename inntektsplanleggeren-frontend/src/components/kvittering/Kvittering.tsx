import {Heading, VStack, Alert, BodyLong, BodyShort, HStack, Loader} from "@navikt/ds-react";
import React, {useContext, useEffect, useState} from "react";
import {FormStateContext} from "@/context/FormData";
import {useParams} from "react-router";

export const Kvittering = () => {
    const { id } = useParams();

    // TODO: Get application by ID and use creation date to determine if the user has waited long.

    const [isWaiting, setIsWaiting] = useState(true);
    const [hasWaitedLong, setHasWaitedLong] = useState(false);
    const { setFormStep } = useContext(FormStateContext);

    useEffect(() => {
        setFormStep(3);
    }, [setFormStep]);

    useEffect(() => {
        const longWaitTimer = setTimeout(() => {
            setHasWaitedLong(true);
        }, 10_000);

        // TODO: Remove fake API timer.
        const fakeProcessTimer = setTimeout(() => {
            setIsWaiting(false);
            clearTimeout(longWaitTimer);
        }, 5_000);

        return () => {
            clearTimeout(fakeProcessTimer);
            clearTimeout(longWaitTimer);
        };
    }, []);

    if (isWaiting) {
        return (
            <VStack className="form-container">
                <Heading level="2" size="small">Vent mens vi behandler innsendingen</Heading>
                <Alert variant="info">
                    <HStack gap="10">
                        <Loader size="3xlarge" title="Venter..." />
                        <VStack>
                            <Heading spacing size="small" level="3">Vi forsøker automatisk behandling </Heading>
                            <BodyShort>Dette kan ta opptil ett minutt. Hvis vi ikke kan behandle innsendingen din automatisk, blir den behandlet av en saksbehandler.</BodyShort>
                        </VStack>
                    </HStack>
                  </Alert>
            </VStack>
        );
    }

    // if (isWaiting) {
    //     return (
    //         <VStack className="form-container">
    //             <Heading level="2" size="small">Vent mens vi behandler innsendingen</Heading>
    //                 <HStack gap="10">
    //                     <Loader size="3xlarge" title="Venter..." />
    //                     <VStack>
    //                         <Heading spacing size="small" level="3">Vi forsøker automatisk behandling </Heading>
    //                         <BodyShort>Dette kan ta opptil ett minutt. Hvis vi ikke kan behandle innsendingen din automatisk, blir den behandlet av en saksbehandler.</BodyShort>
    //                     </VStack>
    //                 </HStack>
    //         </VStack>
    //     );
    // }



    return (
        <VStack className="form-container">
            {hasWaitedLong ? <Alert variant="info">
                <Heading spacing size="small" level="3">Nav har mottatt opplysninger om inntekten din </Heading>
                <BodyShort>
                    Din registrerte forventede inntekt i 2024: 100 000 kr
                </BodyShort>
                <BodyShort>
                    Endringen er sendt til behandling. I de fleste tilfeller vil saken være ferdig behandlet i løpet av 14 dager.
                </BodyShort>
            </Alert> : null}

            <Heading level="2" size="small">Kvittering</Heading>

            {/*<Alert variant="success">*/}
            {/*    <Heading spacing size="small" level="3">Viktig informasjon</Heading>*/}
            {/*    <BodyShort>Søknaden din hart blitt sendt</BodyShort>*/}
            {/*</Alert>*/}

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


