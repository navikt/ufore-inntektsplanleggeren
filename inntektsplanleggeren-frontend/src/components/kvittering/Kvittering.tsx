import {Heading, VStack, Alert, BodyLong, BodyShort, HStack, Loader, List} from "@navikt/ds-react";
import React, {useContext, useEffect, useState} from "react";
import {FormStateContext} from "@/context/FormData";
import { DataContext } from "@/DataContextProvider";
import {Link} from "react-router-dom";

export const Kvittering = () => {
    // const { id } = useParams();

    // TODO: Get application by ID and use creation date to determine if the user has waited long.

    const [isWaiting, setIsWaiting] = useState(true);
    const [hasWaitedLong, setHasWaitedLong] = useState(false);
    const { setFormStep } = useContext(FormStateContext);
    const { sendResponse } = useContext(DataContext);

    useEffect(() => {
        setFormStep(3);
    }, [setFormStep]);

    // useEffect(() => {
    //     console.log("sendResponse", sendResponse);
    //     const longWaitTimer = setTimeout(() => {
    //         setHasWaitedLong(true);
    //     }, 10_000);
    //
    //     // TODO: Remove fake API timer.
    //     const fakeProcessTimer = setTimeout(() => {
    //         setIsWaiting(false);
    //         clearTimeout(longWaitTimer);
    //     }, 5_000);
    //
    //     return () => {
    //         clearTimeout(fakeProcessTimer);
    //         clearTimeout(longWaitTimer);
    //     };
    // }, []);

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

            <Heading size={"large"}>Etteroppgjør</Heading>
            <BodyLong>
                Hver høst sjekker vi om du har fått utbetalt riktig beløp. Det gjør vi ved å hente dine inntektsopplysninger fra forrige år, fra blant annet Skatteetaten.
                Har du fått utbetalt for mye, må du betale tilbake. Har du fått utbetalt for lite, betaler vi deg tilbake. Dette kalles etteroppgjør. <Link to={"/"}>Les mer om etteroppgjøret (åpnes i ny fane).</Link>
            </BodyLong>

            <Heading size={"large"}>Hvis inntekten din endrer seg</Heading>
            <BodyLong>
                Ser du at inntekten din blir annerledes enn det du meldte inn her, bør du melde inn ny inntekt så fort som mulig. Det gir mindre risiko for stor tilbakebetaling i etteroppgjøret.
                Du kan melde ny endring i inntektsplanleggeren så mange ganger du trenger i løpet av året.
            </BodyLong>

            <Heading size={"large"}>Husk å oppdatere skattekortet</Heading>
            <BodyLong>
                Hvis du har fått endret inntekt, kan det være at skattekortet ditt må oppdateres.
                <Link to={"/"}> Les om skattekort og endre det hos Skatteetaten (åpnes i ny fane)</Link>
            </BodyLong>

            <Heading size={"large"}>Må du melde fra til flere?</Heading>
            <BodyLong>
                Får du andre utbetalinger fra Nav eller pengestøtter fra andre, kan det hende at du må melde om endring i inntekt til disse også. Det kan for eksempel gjelde
                <List>
                    <List.Item>økonomisk sosialhjelp</List.Item>
                    <List.Item>uførepensjon fra en pensjonskasse eller forsikringsordning</List.Item>
                    <List.Item>bostøtte fra Husbanken</List.Item>
                </List>
            </BodyLong>


        </VStack>
    );
};


