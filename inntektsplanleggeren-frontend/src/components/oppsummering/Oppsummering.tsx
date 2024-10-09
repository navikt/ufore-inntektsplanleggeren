import {BodyLong, Button, FormSummary, Heading, VStack} from "@navikt/ds-react";
import React, {useContext} from "react";
import {Link} from "react-router-dom";
import {FormStateContext} from "@/context/FormData";
import {submitInntektSimulation} from "@/api/apiFetching";
import {SelectedYearContext} from "@/context/SelectedYear";

export const Oppsummering = () => {
    const { formData, getPersonInntektSum, getAnnenForelderInntektSum } = useContext(FormStateContext);
    const { selectedYear } = useContext(SelectedYearContext);

    const { setFormStep } = useContext(FormStateContext);
    setFormStep(3)

    const send = () => submitInntektSimulation(formData, selectedYear); // TODO: Implement.

    return (
        <VStack gap="5">
            <BodyLong>Sjekk at opplysningene du har oppgitt er riktige. [Reskrive det neste] Opplysningene gjelder bare for uføretrygden du får fra oss.
                Hvis du har tjenestepensjon, må du kontakte tjenestepensjonsordningen du tilhører.</BodyLong>
            <FormSummary>
                <FormSummary.Header>        <FormSummary.Heading level="2">Opplysningene du sender inn</FormSummary.Heading>        <FormSummary.EditLink href="#" />      </FormSummary.Header>
                <FormSummary.Answers>
                    <FormSummary.Answer> <FormSummary.Label>Din forventede inntekt i {selectedYear}</FormSummary.Label>  <FormSummary.Value>{getPersonInntektSum}</FormSummary.Value>        </FormSummary.Answer>
                    <FormSummary.Answer> <FormSummary.Label>Annen forelders forventede inntekt i 2024</FormSummary.Label> <FormSummary.Value>{getAnnenForelderInntektSum}</FormSummary.Value>        </FormSummary.Answer>
                </FormSummary.Answers>
            </FormSummary>

            <Button as={Link} to="/beregning" variant="secondary">
                Tilbake
            </Button>
            <Button onClick={() => send()} as={Link} to="/kvittering" variant="primary">
                Send
            </Button>
        </VStack>
    );
};