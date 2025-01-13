import {Alert, BodyLong, Button, Heading, HStack, ReadMore, VStack, Link} from "@navikt/ds-react";
import React, {FormEvent, MouseEvent, useContext, useEffect} from "react";
import {Link as RouterLink, useNavigate} from "react-router-dom";
import {FormStateContext} from "@/context/FormData";
import {SimulationTable} from "@/components/beregning/SimulationTable";
import {DataContext} from "@/DataContextProvider";
import {SelectedYearContext} from "@/context/SelectedYear";
import {getFullPathForPage, PageLinks} from "@/FormContainer";
import {MessageCodes} from "@/api/model/MessageCodes";
import {Graph} from "@/components/beregning/Graph";
import {InputSummary} from "@/components/beregning/InputSummary";
import {ArrowLeftIcon, ArrowRightIcon} from "@navikt/aksel-icons";
import {FormatKroner} from "@/components/utils/FormatKroner";
import {CancelConfirmationModal} from "@/components/common/CancelConfirmationModal";

export const BeregningPage = () => {
    const { setFormStep, brukerinntekt, annenForelderInntekt } = useContext(FormStateContext);
    const { simulationResponse } = useContext(DataContext);
    const { selectedYear } = useContext(SelectedYearContext);
    const navigate = useNavigate();

    useEffect(() => {
        setFormStep(2)
    }, [setFormStep]);

    const handleSubmit = async (e: MouseEvent | FormEvent) => {
        e.preventDefault();
        navigate(getFullPathForPage(PageLinks.OPPSUMMERING));
    }

    const showSimulering = !(simulationResponse?.messages.some(message => message.messageCode === MessageCodes.FAKTOROMREGNET_ELLER_MANUELT_OVERSTYRT
        || message.messageCode === MessageCodes.SIMULERING_CONTAINS_MOTREGNING))

    if (simulationResponse?.result) return (
        <VStack gap="8">
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

            { simulationResponse.messages.some(message => message.messageCode === MessageCodes.OPPGITT_INNTEKT_OVER_INNTEKTSTAK) ?
                <Alert variant="warning">
                    Inntekten du har lagt inn er høyere enn 80 prosent av inntekten du hadde før du ble ufør. Derfor får du ikke utbetalt uføretrygd resten av året. Har du fortsatt fått utbetalt for
                    mye når året er slutt, kan du få et etteroppgjør hvor du må betale tilbake.
                </Alert> : null
            }

            { simulationResponse.messages.some(message => message.messageCode === MessageCodes.OPPGITT_INNTEKT_GIVES_LOWER_UFORETRYGD_THAN_ALREADY_UTBETALT) ?
                <Alert variant="warning">
                    Inntekten du har lagt inn, er høyere enn inntekten vi har brukt til å beregne uføretrygden din. Din nye inntekt viser at du har fått utbetalt for mye uføretrygd. Derfor kan det være at du
                    ikke får utbetalt uføretrygd resten av året. Får du barnetillegg, kan det hende at du ikke får det utbetalt resten av året. Har du fortsatt fått for mye utbetalt ved slutten av året,
                    kan du få et etteroppgjør hvor du må betale tilbake.
                </Alert> : null
            }

            { simulationResponse.messages.some(message => message.messageCode === MessageCodes.OPPGITT_INNTEKT_GIVES_MORE_UFORETRYGD_THAN_ALREADY_UTBETALT) ?
                <Alert variant="info">
                    Inntekten du nå har lagt inn, er lavere enn inntekten vi har brukt til å beregne uføretrygden din. Din nye inntekt viser at du har fått utbetalt for lite uføretrygd.
                    Derfor vil du ikke få trekk i uføretrygden din resten av året. Har du fortsatt fått for lite utbetalt ved slutten av året, kan du få tilbake penger i etteroppgjøret.
                </Alert> : null
            }

            {showSimulering ?
                <>
                    <VStack gap="3">
                        <Heading size={"medium"}>Oversikt i graf</Heading>
                        <Graph simulationResult={simulationResponse?.result}/>
                    </VStack>

                    <VStack>
                        <Heading size={"medium"}>Oversikt i tabell</Heading>
                        <VStack gap="12">
                            {simulationResponse?.result &&
                                <SimulationTable simulationResult={simulationResponse.result}></SimulationTable>}
                            <VStack>
                                <BodyLong><strong>Månedlig utbetaling av uføretrygd med dine endringer, før
                                    skatt: <FormatKroner
                                        value={simulationResponse?.result.sum.monthly.after ?? 0}/></strong></BodyLong>
                                <ReadMore header="Månedsbeløp spesifisert">
                                    <VStack>
                                        <BodyLong>{simulationResponse?.result?.gjenlevendetillegg ? "Uføretrygd inkludert gjenlevendetillegg: " : "Uføretrygd: "}
                                            <FormatKroner
                                                value={(simulationResponse?.result?.uforetrygd.monthly.after ?? 0) + (simulationResponse?.result.gjenlevendetillegg?.monthly.after ?? 0)}/></BodyLong>
                                        {simulationResponse?.result.barnetilleggFellesbarn !== null ?
                                            <BodyLong>Barnetillegg for fellesbarn: <FormatKroner
                                                value={(simulationResponse?.result.barnetilleggFellesbarn.monthly.after ?? 0)}/></BodyLong> : null}
                                        {simulationResponse?.result.barnetilleggSaerkullsbarn !== null ?
                                            <BodyLong>Barnetillegg for særkullsbarn: <FormatKroner
                                                value={(simulationResponse?.result.barnetilleggSaerkullsbarn.monthly.after ?? 0)}/></BodyLong> : null}
                                    </VStack>
                                </ReadMore>
                            </VStack>
                        </VStack>
                    </VStack>
                </>
                :
                <Alert variant="warning">
                    Vi kan dessverre ikke vise hvordan den nye inntekten din vil påvirke utbetalingen din av uføretrygd.
                    Du kan likevel sende inn din inntektsendring.
                </Alert>
            }
            
            <BodyLong><strong>Har du spørsmål? <Link href={PageLinks.KONTAKT} target="_blank">Kontakt oss (åpnes i ny fane)</Link></strong></BodyLong>

            <HStack gap="4">
                <Button as={RouterLink} to={getFullPathForPage(PageLinks.FORVENTEDE_INNTEKTER)} iconPosition="left" icon={<ArrowLeftIcon aria-hidden />} variant="secondary">
                    Endre beløp i beregning
                </Button>
                <Button variant="primary" iconPosition="right" icon={<ArrowRightIcon aria-hidden/>}onClick={handleSubmit}>
                    Gå til innsending
                </Button>
            </HStack>
            <HStack>
                <CancelConfirmationModal/>
            </HStack>
        </VStack>
    );
};
