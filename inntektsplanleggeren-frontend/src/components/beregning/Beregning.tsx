import {Button, Heading} from "@navikt/ds-react";
import React, {useContext} from "react";
import {Link} from "react-router-dom";

export const Beregning = () => {
    return (
        <>
            {/*<Heading level="2" size="small">Din inntekt og uføretrygd før skatt i {year}</Heading>*/}

            <Button as={Link} to="/forventede-inntekter" variant="secondary">
                Tilbake
            </Button>
            <Button as={Link} to="/oppsummering" variant="primary">
                Oppsummering
            </Button>
        </>
    );
};