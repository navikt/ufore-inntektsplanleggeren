import {Button, Heading, HStack, VStack} from "@navikt/ds-react";
import React, {useContext, useEffect} from "react";
import {Link} from "react-router-dom";
import {FormStateContext} from "@/context/FormData";
import {SimulationTable} from "@/components/oppsummering/SimulationTable";
import {basePath} from "@/routes";

export const Oppsummering = () => {
    const { setFormStep, simulationInntekt, selectedYear } = useContext(FormStateContext);

    useEffect(() => {
        setFormStep(2)
    }, [setFormStep]);

    return (
        <VStack gap="4">
            <Heading size={"large"}>Din inntekt og uføretrygd før skatt i {selectedYear}</Heading>

            <VStack gap="6">
                <Heading size={"large"}>Detaljert oversikt før skatt 2024</Heading>
                { simulationInntekt && <SimulationTable simulationResult={simulationInntekt}></SimulationTable>}
            </VStack>

            <HStack gap="4">
                <Button as={Link} to={basePath + "/forventede-inntekter"} variant="secondary">
                    Tilbake
                </Button>
                <Button as={Link} to={basePath + "/kvittering"} variant="primary">
                    Send inn
                </Button>
            </HStack>
        </VStack>
    );
};
