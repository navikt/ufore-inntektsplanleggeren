import {Button, Heading, VStack} from "@navikt/ds-react";
import React, {useContext} from "react";
import {Link} from "react-router-dom";
import {FormStateContext} from "@/context/FormData";

export const Beregning = () => {

    const { setFormStep } = useContext(FormStateContext);
    setFormStep(2)

    return (
        <VStack>
            {/*<Heading level="2" size="small">Din inntekt og uføretrygd før skatt i </Heading>*/}

            <Button as={Link} to="/forventede-inntekter" variant="secondary">
                Tilbake
            </Button>
            <Button as={Link} to="/oppsummering" variant="primary">
                Send inn
            </Button>
        </VStack>
    );
};