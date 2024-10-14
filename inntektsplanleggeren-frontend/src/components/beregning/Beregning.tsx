import {Button, Heading, VStack} from "@navikt/ds-react";
import React, {useContext} from "react";
import {Link} from "react-router-dom";
import {FormStateContext} from "@/context/FormData";
import {SimulationTable} from "@/components/oppsummering/SimulationTable";

export const Beregning = () => {
    React.useEffect(() => {
        window.scrollTo(0, 0);
    }, []);

    const { setFormStep, simulationInntekt, selectedYear } = useContext(FormStateContext);
    setFormStep(2)

    return (
        <VStack>
            <Heading size={"large"}>Din inntekt og uføretrygd før skatt i {selectedYear}</Heading>
            {/*<Heading level="2" size="small">Din inntekt og uføretrygd før skatt i </Heading>*/}
            <Heading size={"large"}>Oversikt i tabell</Heading>
            { simulationInntekt && <SimulationTable simulationResult={simulationInntekt}></SimulationTable>}

            <Button as={Link} to="/forventede-inntekter" variant="secondary">
                Tilbake
            </Button>
            <Button as={Link} to="/oppsummering" variant="primary">
                Send inn
            </Button>
        </VStack>
    );
};