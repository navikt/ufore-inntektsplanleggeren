import {Heading, VStack, Alert, BodyLong, BodyShort, HStack, Loader, List, Button} from "@navikt/ds-react";
import React, {useContext, useEffect, useState} from "react";
import {FormStateContext} from "@/context/FormData";
import { DataContext } from "@/DataContextProvider";
import {Link} from "react-router-dom";
import {KvitteringStatusBox} from "@/components/kvittering/KvitteringStatusBox";
import {getStatus} from "@/api/apiFetching";
import {PageLinks} from "@/form-container";

export const KvitteringPage = () => {
    // const { id } = useParams();

    // TODO: Get application by ID and use creation date to determine if the user has waited long.

    const [isWaiting, setIsWaiting] = useState(true);
    const { setFormStep, getBrukerinntektSum, getAnnenForelderInntektSum, selectedYear } = useContext(FormStateContext);
    const { statusResponse, setStatusResponse, sendResponse } = useContext(DataContext);

    useEffect(() => {
        setFormStep(4);
    }, [setFormStep]);


    useEffect(() => {
        let attempts = 0;
        setIsWaiting(true);
        const intervalId = setInterval(() => {
            if (attempts >= 5) {
                console.log("attempts exceeded", attempts);
                setIsWaiting(false);
                clearInterval(intervalId);
                return;
            }
            if (selectedYear && sendResponse?.innsendingsTidspunkt) {
                getStatus(selectedYear, sendResponse.innsendingsTidspunkt).then(result => {
                    setStatusResponse(result);
                    if (result.status === "BEHANDLET_MEDFOERER_ENDRING" || result.status === "BEHANDLET_MEDFOERER_INGEN_ENDRING") {
                        setIsWaiting(false);
                        clearInterval(intervalId);
                    }
                });
            }
            attempts++;
        }, 1_000);

        return () => {
            clearInterval(intervalId);
        };
    }, []);

    if (isWaiting) {
        return (
            <VStack className="form-container" align="center" gap="20">
                    <Heading level="2" size="large">Vent mens vi sender inn</Heading>
                        <Loader size="3xlarge"/>
                        <VStack>
                            <BodyShort>Dette kan ta opptil ett minutt.</BodyShort>
                        </VStack>
            </VStack>
        );
    }

    return (
        <VStack className="form-container">
            <Heading level="2" size="large">Kvittering</Heading>


            { statusResponse ? <KvitteringStatusBox statusResponse={statusResponse} registeredInntekt={getBrukerinntektSum()} epsRegisteredInntekt={getAnnenForelderInntektSum()} /> : null }

            <Heading size={"large"}>Etteroppgjør</Heading>
            <BodyLong>
                Hver høst sjekker vi om du har fått utbetalt riktig beløp. Det gjør vi ved å hente dine inntektsopplysninger fra forrige år, fra blant annet Skatteetaten.
                Har du fått utbetalt for mye, må du betale tilbake. Har du fått utbetalt for lite, betaler vi deg tilbake. Dette kalles etteroppgjør. <Link to={"/"} target="_blank">Les mer om etteroppgjøret (åpnes i ny fane).</Link> {/* TODO link? */}
            </BodyLong>

            <Heading size={"large"}>Hvis inntekten din endrer seg</Heading>
            <BodyLong>
                Ser du at inntekten din blir annerledes enn det du meldte inn her, bør du melde inn ny inntekt så fort som mulig. Det gir mindre risiko for stor tilbakebetaling i etteroppgjøret.
                Du kan melde ny endring i inntektsplanleggeren så mange ganger du trenger i løpet av året.
            </BodyLong>

            <Heading size={"large"}>Husk å oppdatere skattekortet</Heading>
            <BodyLong>
                Hvis du har fått endret inntekt, kan det være at skattekortet ditt må oppdateres. <Link to={"/"}>Les om skattekort og endre det hos Skatteetaten (åpnes i ny fane)</Link>
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

            <HStack gap="4">
            <Button as={Link} to={PageLinks.DIN_UFORE} variant="primary">Din uføretrygd</Button>
            </HStack>
        </VStack>
    );
};