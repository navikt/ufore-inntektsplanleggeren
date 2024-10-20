import {Button, Heading, HStack, VStack} from "@navikt/ds-react";
import React, {useContext} from "react";
import {Link} from "react-router-dom";
import {FormStateContext} from "@/context/FormData";
import {SimulationTable} from "@/components/oppsummering/SimulationTable";

export const Oppsummering = () => {
    React.useEffect(() => {
        window.scrollTo(0, 0);
    }, []);

    const { setFormStep, simulationInntekt, selectedYear } = useContext(FormStateContext);
    setFormStep(2)

    return (
        <VStack gap="4">
            <Heading size={"large"}>Din inntekt og uføretrygd før skatt i {selectedYear}</Heading>
            {/*<Heading level="2" size="small">Din inntekt og uføretrygd før skatt i </Heading>*/}

            <VStack gap="6">
                <Heading size={"large"}>Detaljert oversikt før skatt 2024</Heading>
                { simulationInntekt && <SimulationTable simulationResult={simulationInntekt}></SimulationTable>}
            </VStack>

            <HStack gap="4">
                <Button as={Link} to="/forventede-inntekter" variant="secondary">
                    Tilbake
                </Button>
                <Button as={Link} to="/kvittering" variant="primary">
                    Send inn
                </Button>
            </HStack>
        </VStack>
    );
};