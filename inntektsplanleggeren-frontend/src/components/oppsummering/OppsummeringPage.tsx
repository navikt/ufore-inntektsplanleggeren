import {Alert, BodyLong, Button, Heading, HStack, ReadMore, VStack, Link} from "@navikt/ds-react";
import React, {FormEvent, MouseEvent, useContext, useEffect, useState} from "react";
import {Link as RouterLink, useNavigate} from "react-router-dom";
import {FormStateContext} from "@/context/FormData";
import {SimulationTable} from "@/components/oppsummering/SimulationTable";
import {DataContext} from "@/DataContextProvider";
import {send} from "@/api/apiFetching";
import {SelectedYearContext} from "@/context/SelectedYear";
import {PageLinks} from "@/form-container";
import {MessageTypes} from "@/api/model/MessageCodes";
import {Graph} from "@/components/oppsummering/Graph";
import {InputSummary} from "@/components/oppsummering/InputSummary";
import {ArrowLeftIcon, ArrowRightIcon} from "@navikt/aksel-icons";
import {FormatKroner} from "@/components/utils/FormatKroner";

export const OppsummeringPage = () => {
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

                <VStack gap="7">
                    <VStack>
                        <Heading size="small">Dine forventede inntekter i {selectedYear}</Heading>
                        <InputSummary inntekter={brukerinntekt}></InputSummary>
                    </VStack>
                    {annenForelderInntekt ?
                        <VStack>
                            <Heading size="small">Annen forelder forventet inntekt i {selectedYear}</Heading>
                            <InputSummary inntekter={annenForelderInntekt}></InputSummary>
                        </VStack> : null }
                    </VStack>
            </ReadMore>

            <Heading size={"medium"}>Oversikt i graf</Heading>
            <Graph></Graph>

            <VStack gap="6">
                <Heading size={"large"}>Detaljert oversikt før skatt 2024</Heading>
                {simulationResponse?.result && <SimulationTable simulationResult={simulationResponse.result}></SimulationTable>}
            </VStack>

            <BodyLong>Månedlig utbetaling av uføretrygd med dine endringer, før skatt:</BodyLong>
            {/*TODO what here?*/}

            <ReadMore header="Månedsbeløp spesifisert">
                <VStack>
                    <BodyLong>Uføretrygd inkludert gjenlevendetillegg: <FormatKroner value={(simulationResponse?.result.uforetrygd?.monthly.after ?? 0) + (simulationResponse?.result.gjenlevendetillegg?.monthly.after ?? 0)}/></BodyLong>
                    { simulationResponse?.result.barnetilleggFellesbarn !== null ? <BodyLong>Barnetillegg for fellesbarn: <FormatKroner value={(simulationResponse?.result.barnetilleggFellesbarn.monthly.after ?? 0)}/></BodyLong> : null}
                    { simulationResponse?.result.barnetilleggSaerkullsbarn !== null ? <BodyLong>Barnetillegg for særkullsbarn: <FormatKroner value={(simulationResponse?.result.barnetilleggSaerkullsbarn.monthly.after ?? 0)}/></BodyLong> : null}
                    { simulationResponse?.result.gjenlevendetillegg !== null ?
                        <BodyLong>Gjenlevendetillegg: <FormatKroner value={simulationResponse?.result.gjenlevendetillegg?.monthly.after ?? 0}/></BodyLong> : null }
                </VStack>
            </ReadMore>
            
            <BodyLong><strong>Har du spørsmål? <Link href={PageLinks.KONTAKT} target="_blank">Kontakt oss (åpnes i ny fane)</Link></strong></BodyLong>

            <HStack gap="4">
                <Button as={RouterLink} to={PageLinks.FORVENTEDE_INNTEKTER} iconPosition="left" icon={<ArrowLeftIcon aria-hidden />} variant="secondary">
                    Endre beløp i beregning
                </Button>
                <Button variant="primary" iconPosition="right" icon={<ArrowRightIcon aria-hidden/>}onClick={handleSubmit} loading={isLoading}>
                    Gå til innsending
                </Button>
            </HStack>
        </VStack>
    );
};
