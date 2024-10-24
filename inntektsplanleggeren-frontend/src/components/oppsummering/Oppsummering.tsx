import {Button, Heading, HStack, VStack} from "@navikt/ds-react";
import React, {useContext, useEffect, useState} from "react";
import {Link, useNavigate} from "react-router-dom";
import {FormStateContext} from "@/context/FormData";
import {SimulationTable} from "@/components/oppsummering/SimulationTable";
import {DataContext} from "@/DataContextProvider";

export const Oppsummering = () => {
    const { setFormStep, selectedYear } = useContext(FormStateContext);
    const { simulationResponse } = useContext(DataContext);
    const navigate = useNavigate();
    const [isLoading, setIsLoading] = useState(false);

    useEffect(() => {
        setFormStep(2)
    }, [setFormStep]);

    const onSend = async () => {
        setIsLoading(true);
        const uuid = await fakeApiCall(); // TODO: Replace with actual API call.
        navigate(`/${uuid}/kvittering`);
    };


    return (
        <VStack gap="4">
            <Heading size={"large"}>Din inntekt og uføretrygd før skatt i {selectedYear}</Heading>

            <VStack gap="6">
                <Heading size={"large"}>Detaljert oversikt før skatt 2024</Heading>
                {simulationResponse?.result && <SimulationTable simulationResult={simulationResponse.result}></SimulationTable>}
            </VStack>

            <HStack gap="4">
                <Button as={Link} to="/forventede-inntekter" variant="secondary">
                    Tilbake
                </Button>
                <Button variant="primary" onClick={onSend} loading={isLoading}>
                    Send inn
                </Button>
            </HStack>
        </VStack>
    );
};

// TODO: Remove API call mock.
const fakeApiCall = async () => {
    return new Promise<string>((resolve) => {
        setTimeout(() => {
            resolve(crypto.randomUUID());
        }, 500);
    });
};
