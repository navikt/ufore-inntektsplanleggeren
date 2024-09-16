import {Button, Heading} from "@navikt/ds-react";
import React, {useContext} from "react";
import {Link} from "react-router-dom";
import {FormStateContext} from "@/SelectedYear/SelectedYear";

export const Oppsummering = () => {
    const { selectedYear, setSelectedYear } = useContext(FormStateContext);

    const send = () => undefined; // TODO: Implement.

    return (
        <>
            <Heading level="2" size="small">Din inntekt og uføretrygd før skatt i {selectedYear}</Heading>
            <Button onClick={() => setSelectedYear((year) => year + 1)}>Increase Year</Button>

            <Button as={Link} to="/beregning" variant="secondary">
                Tilbake
            </Button>
            <Button onClick={() => send()} variant="primary">
                Send
            </Button>
        </>
    );
};