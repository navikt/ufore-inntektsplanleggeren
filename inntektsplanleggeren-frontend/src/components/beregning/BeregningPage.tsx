import {Alert, BodyLong, Button, Heading, HStack, ReadMore, VStack, Link} from "@navikt/ds-react";
import React, {FormEvent, MouseEvent, useContext, useEffect, useState} from "react";
import {Link as RouterLink, useNavigate} from "react-router-dom";
import {FormStateContext} from "@/context/FormData";
import {SimulationTable} from "@/components/beregning/SimulationTable";
import {DataContext} from "@/DataContextProvider";
import {send} from "@/api/apiFetching";
import {SelectedYearContext} from "@/context/SelectedYear";
import {PageLinks} from "@/form-container";
import {MessageCodes} from "@/api/model/MessageCodes";
import {Graph} from "@/components/beregning/Graph";
import {InputSummary} from "@/components/beregning/InputSummary";
import {ArrowLeftIcon, ArrowRightIcon} from "@navikt/aksel-icons";
import {FormatKroner} from "@/components/utils/FormatKroner";
import {CancelConfirmationModal} from "@/components/common/CancelConfirmationModal";

export const BeregningPage = () => {
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

    if (simulationResponse?.result) return (
        <VStack gap="5">
            { simulationResponse.messages.some(message => message.messageCode === MessageCodes.USER_HAS_NO_LOPENDE_VEDTAK_YET) ?
                <Alert variant="warning">
                    Du kan ikke bruke inntektsplanleggeren ennå. Din inntekt kan registreres her fra måneden før din første utbetaling av uføretrygd.
                </Alert> : null
            }

            <Heading size={"large"}>Din inntekt og uføretrygd før skatt i {selectedYear}</Heading>

            <ReadMore header="Inntekten du har lagt inn">

                <VStack gap="7">
                    <VStack>
                        <Heading size="small">Dine forventede inntekter i {selectedYear}</Heading>
                        <InputSummary inntekter={brukerinntekt}></InputSummary>
                    </VStack>
                    {annenForelderInntekt ?
                        <VStack>
                            <Heading size="small">Annen forelders forventede inntekt i {selectedYear}</Heading>
                            <InputSummary inntekter={annenForelderInntekt}></InputSummary>
                        </VStack> : null }
                    </VStack>
            </ReadMore>

            <VStack gap="3">
                <Heading size={"medium"}>Oversikt i graf</Heading>
                <Graph simulationResult={simulationResponse?.result}/>
            </VStack>

            <VStack>
                <Heading size={"medium"}>Oversikt i tabell</Heading>
                {simulationResponse?.result && <SimulationTable simulationResult={simulationResponse.result}></SimulationTable>}
            </VStack>

            <BodyLong><strong>Månedlig utbetaling av uføretrygd med dine endringer, før skatt: <FormatKroner value={simulationResponse?.result.sum.monthly.after ?? 0}/></strong></BodyLong>
            {/*TODO what here?*/}

            <ReadMore header="Månedsbeløp spesifisert">
                <VStack>
                    <BodyLong>{ simulationResponse?.result?.gjenlevendetillegg ? "Uføretrygd inkludert gjenlevendetillegg: " : "Uføretrygd: "}
                        <FormatKroner value={(simulationResponse?.result?.uforetrygd.monthly.after ?? 0) + (simulationResponse?.result.gjenlevendetillegg?.monthly.after ?? 0)}/></BodyLong>
                    { simulationResponse?.result.barnetilleggFellesbarn !== null ? <BodyLong>Barnetillegg for fellesbarn: <FormatKroner value={(simulationResponse?.result.barnetilleggFellesbarn.monthly.after ?? 0)}/></BodyLong> : null}
                    { simulationResponse?.result.barnetilleggSaerkullsbarn !== null ? <BodyLong>Barnetillegg for særkullsbarn: <FormatKroner value={(simulationResponse?.result.barnetilleggSaerkullsbarn.monthly.after ?? 0)}/></BodyLong> : null}
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
            <HStack>
                <CancelConfirmationModal/>
            </HStack>
        </VStack>
    );
};
