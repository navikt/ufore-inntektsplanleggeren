import {Button, Heading} from "@navikt/ds-react";
import React, {useContext} from "react";
import {FormStateContext} from "@/form-container";
import {Link} from "react-router-dom";

export const Beregning = () => {
    const { year, setYear } = useContext(FormStateContext);

    return (
        <>
            <Heading level="2" size="small">Din inntekt og uføretrygd før skatt i {year}</Heading>
            <Button onClick={() => setYear((year) => year + 1)}>Increase Year</Button>

            <Button as={Link} to="/forventede-inntekter" variant="secondary">
                Tilbake
            </Button>
            <Button as={Link} to="/oppsummering" variant="primary">
                Oppsummering
            </Button>
        </>
    );
};