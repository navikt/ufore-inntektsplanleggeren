import {Alert, Button, Heading, HStack, ReadMore, VStack} from "@navikt/ds-react";
import React, {FormEvent, MouseEvent, useContext, useEffect, useState} from "react";
import {Link, useNavigate} from "react-router-dom";
import {FormStateContext} from "@/context/FormData";
import {SimulationTable} from "@/components/oppsummering/SimulationTable";
import {DataContext} from "@/DataContextProvider";
import {send} from "@/api/apiFetching";
import {SelectedYearContext} from "@/context/SelectedYear";
import {PageLinks} from "@/form-container";
import {MessageTypes} from "@/api/model/MessageCodes";
import {Graph} from "@/components/oppsummering/Graph";
import {InputSummary} from "@/components/oppsummering/InputSummary";

export const Oppsummering = () => {
    const { setFormStep, brukerinntekt, annenForelderInntekt } = useContext(FormStateContext);
    const { simulationResponse, setSendResponse } = useContext(DataContext);
    const { selectedYear } = useContext(SelectedYearContext);
    const navigate = useNavigate();
    const [isLoading, setIsLoading] = useState(false);

    useEffect(() => {
        setFormStep(2)
    }, [setFormStep]);

    const handleSubmit = async (e: MouseEvent | FormEvent) => {
        e.preventDefault();

        try {
            setIsLoading(true);
            const result = await send(brukerinntekt, annenForelderInntekt, selectedYear);
            setSendResponse(result);
            navigate(PageLinks.KVITTERING);
        } catch (error) {
            console.error("Error submitting income simulation:", error);
        }

        navigate(PageLinks.KVITTERING);
    }

    return (
        <VStack gap="4">
            { simulationResponse?.messages.map((message, index) => (
                <Alert key={index} variant={message.details === MessageTypes.ERROR ? "error" : "warning"}>{message.details}</Alert>
            ))}

            <Heading size={"medium"}>Din inntekt og uføretrygd før skatt i {selectedYear}</Heading>

            <ReadMore header="Inntekten du har lagt inn">
                <InputSummary inntekter={brukerinntekt}></InputSummary>
            </ReadMore>

            <Graph></Graph>



            <VStack gap="6">
                <Heading size={"large"}>Detaljert oversikt før skatt 2024</Heading>
                {simulationResponse?.result && <SimulationTable simulationResult={simulationResponse.result}></SimulationTable>}
            </VStack>

            <HStack gap="4">
                <Button as={Link} to="/forventede-inntekter" variant="secondary">
                    Tilbake
                </Button>
                <Button variant="primary" onClick={handleSubmit} loading={isLoading}>
                    Send inn
                </Button>
            </HStack>
        </VStack>
    );
};
