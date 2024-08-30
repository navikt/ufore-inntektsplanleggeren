import {Button, Heading} from "@navikt/ds-react";
import React, {useContext} from "react";
import {FormStateContext} from "@/form-container";
import {Link} from "react-router-dom";

export const Oppsummering = () => {
    const { year, setYear } = useContext(FormStateContext);

    const send = () => undefined; // TODO: Implement.

    return (
        <>
            <Heading level="2" size="small">Din inntekt og uføretrygd før skatt i {year}</Heading>
            <Button onClick={() => setYear((year) => year + 1)}>Increase Year</Button>

            <Button as={Link} to="/beregning" variant="secondary">
                Tilbake
            </Button>
            <Button onClick={() => send()} variant="primary">
                Send
            </Button>
        </>
    );
};