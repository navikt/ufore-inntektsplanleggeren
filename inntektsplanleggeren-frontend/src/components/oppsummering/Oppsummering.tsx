import {BodyLong, Button, FormSummary, Heading} from "@navikt/ds-react";
import React, {useContext} from "react";
import {Link} from "react-router-dom";
import {FormStateContext} from "@/context/FormData";

export const Oppsummering = () => {
    const { selectedYear, personInntektSum, annenForelderInntektSum } = useContext(FormStateContext);

    const { setFormStep } = useContext(FormStateContext);
    setFormStep(2)

    const send = () => undefined; // TODO: Implement.

    return (
        <>
            <BodyLong>Sjekk at opplysningene du har oppgitt er riktige. [Reskrive det neste] Opplysningene gjelder bare for uføretrygden du får fra oss.
                Hvis du har tjenestepensjon, må du kontakte tjenestepensjonsordningen du tilhører.</BodyLong>
            <FormSummary>
                <FormSummary.Header>        <FormSummary.Heading level="2">Opplysningene du sender inn</FormSummary.Heading>        <FormSummary.EditLink href="#" />      </FormSummary.Header>
                <FormSummary.Answers>
                    <FormSummary.Answer> <FormSummary.Label>Din forventede inntekt i {selectedYear}</FormSummary.Label>  <FormSummary.Value>{personInntektSum}</FormSummary.Value>        </FormSummary.Answer>
                    <FormSummary.Answer> <FormSummary.Label>Annen forelders forventede inntekt i 2024</FormSummary.Label> <FormSummary.Value>{annenForelderInntektSum}</FormSummary.Value>        </FormSummary.Answer>
                </FormSummary.Answers>
            </FormSummary>

            <Button as={Link} to="/beregning" variant="secondary">
                Tilbake
            </Button>
            <Button onClick={() => send()} variant="primary">
                Send
            </Button>
        </>
    );
};