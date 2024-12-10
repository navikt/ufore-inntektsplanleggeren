import {
    BodyLong,
    Box,
    Button,
    Heading,
    HStack,
    List,
    Loader,
    VStack,
    Alert,
    Link,
    ErrorSummary
} from "@navikt/ds-react";
import React, {FormEvent, MouseEvent, useContext, useEffect, useState} from "react";
import "./innfylling.css"
import {Link as RouterLink, useNavigate} from "react-router-dom";
import {FormStateContext} from "@/context/FormData";
import { FormFieldsUser } from "./FormFieldsUser";
import {simulate} from "@/api/apiFetching";
import {DinInntektTable} from "@/components/innfylling/DinInntektTable";
import {SelectedYearContext} from "@/context/SelectedYear";
import {belopSum} from "@/common/Utils";
import {DataContext} from "@/DataContextProvider";
import {PersonInntekter, SimulationResponse} from "@/api/model/ApiRequests";
import {FormatKroner} from "@/components/utils/FormatKroner";
import {ArrowLeftIcon, ArrowRightIcon} from "@navikt/aksel-icons";
import {PageLinks} from "@/FormContainer";
import {FormFieldsEps} from "@/components/innfylling/FormFieldsEps";
import {CancelConfirmationModal} from "@/components/common/CancelConfirmationModal";
import {MessageCodes} from "@/api/model/MessageCodes";



export const InnfyllingPage = () => {
    const navigate = useNavigate()
    const { brukerinntekt, setBrukerinntekt, annenForelderInntekt, setAnnenForelderInntekt, getBrukerinntektSum, getAnnenForelderInntektSum, setFormStep } = useContext(FormStateContext);
    const { inntekterResponse, setSimulationResponse } = useContext(DataContext);
    const { selectedYear } = useContext(SelectedYearContext);
    const [brukerErrors, setBrukerErrors] = useState<Partial<Record<keyof PersonInntekter, string>>>({});
    const [epsErrors, setEpsErrors] = useState<Partial<Record<keyof PersonInntekter, string>>>({});
    const [sendingErrors, setSendingErrors] = useState<string[]>([]);
    const [isLoading, setIsLoading] = useState<boolean>(false);

    useEffect(() => {
        setFormStep(1);
    }, [setFormStep]);

    const checkForSendingErrors = (response: SimulationResponse) : boolean => {
        const errors: string[] = [];
        const bErrors: Partial<Record<keyof PersonInntekter, string>> = {};
        const eErrors: Partial<Record<keyof PersonInntekter, string>> = {};
        
        //todo review texts below
        for (const message of response.messages) {
            if (message.messageCode === MessageCodes.ARBEIDSINNTEKT_GIVEN_SMALLER_THAN_HITTIL_I_AAR) {
                if(message.metadata["AFFECTED_FIELD"] === "ARBEIDSINNTEKT_BRUKER") {
                    errors.push(`Beløpet for inntekt og pengestøtter kan ikke være mindre enn ${message.metadata["SUM_HITTIL_I_AAR"]}`);
                    bErrors["arbeidsinntekt"] = `Beløpet kan ikke være mindre enn ${message.metadata["SUM_HITTIL_I_AAR"]}, fordi du allerede har fått dette i lønn og pengestøtte`
                } else if(message.metadata["AFFECTED_FIELD"] === "ARBEIDSINNTEKT_EPS") {
                    errors.push(`Inntekt til annen forelder må være høyere enn det hen har tjent hittil i år.`);
                    eErrors["arbeidsinntekt"] = `Beløpet må være høyere enn det den andre forelderen har fått i lønn og pengestøtte hittil i år. Den andre forelderen kan se inntekter som er registrert hittil i år hos Skatteetaten.`;
                }
            }
            else if (message.messageCode === MessageCodes.ANDRE_YTELSER_SMALLER_THAN_HITTIL_I_AAR) {
                if(message.metadata["AFFECTED_FIELD"] === "ANDRE_YTELSER_BRUKER") {
                    errors.push(`Beløpet kan ikke være mindre enn ${message.metadata["SUM_HITTIL_I_AAR"]}`)
                    bErrors["andrePensjonsgivendeYtelser"] = `Beløpet kan ikke være mindre enn ${message.metadata["SUM_HITTIL_I_AAR"]}, fordi du allerede har fått dette i pensjoner fra andre enn folketrygden hittil i år.`;
                } else if(message.metadata["AFFECTED_FIELD"] === "ANDRE_YTELSER_EPS") {
                    errors.push(`Pensjoner til annen forelder må være høyere enn det hen har fått hittil i år.`);
                    eErrors["andrePensjonsgivendeYtelser"] = `Beløpet må være høyere enn det den andre forelderen har fått i pensjoner hittil i år. Den andre forelderen kan se inntekter som er registrert hittil i år hos Skatteetaten.`;
                }
            }
        }

        setSendingErrors(errors);
        setBrukerErrors(bErrors);
        setEpsErrors(eErrors);
        return errors.length > 0;
    }


    const handleSubmit = async (e: MouseEvent | FormEvent) => {
        e.preventDefault();

        try {
            setIsLoading(true);
            const result = await simulate(brukerinntekt, annenForelderInntekt, selectedYear);
            if(checkForSendingErrors(result)) {
                setIsLoading(false);
            } else {
                setIsLoading(false);
                setSimulationResponse(result);
                navigate(PageLinks.BEREGNING);
            }
        } catch (error) {
            console.error("Error submitting income simulation:", error);
        }

        // navigate(PageLinks.BEREGNING);
    };

    if(inntekterResponse === null) {
        return <Loader />;
    }


    return (
        <VStack className="form-container">
            <Heading level="2" size="medium">Din inntekt hittil i år</Heading>
            <BodyLong> Under kan du se hvilke inntekter som er registrert via A-meldingen. Det er likevel viktig at du sender inn forventet inntekt for hele året til oss.
                Når vi får registrert riktig inntekt, kan vi gjøre en riktig beregning av din utbetaling.</BodyLong>

            {(inntekterResponse.arbeidsinntektOgYtelserHittilIAar?.length > 0) &&
                <DinInntektTable data={inntekterResponse.arbeidsinntektOgYtelserHittilIAar} type="arbeidsgiver">
                    <BodyLong>Vi har registrert at du har fått <strong><FormatKroner value={belopSum(inntekterResponse.arbeidsinntektOgYtelserHittilIAar)}/></strong> i arbeidsinntekt og pengestøtter hittil i år.</BodyLong>
                </DinInntektTable>
            }
            {(inntekterResponse.pensjonFraAndreHittilIAar?.length > 0) &&
                <DinInntektTable data={inntekterResponse.pensjonFraAndreHittilIAar} type="pensjonsordning">
                    <BodyLong>Vi har registrert at du har fått <strong><FormatKroner value={belopSum(inntekterResponse.pensjonFraAndreHittilIAar)}/></strong> i pensjoner fra andre enn folketrygden hittil i år.</BodyLong>
                </DinInntektTable>
            }

            <Heading level="2" size="medium">Slik skal du oppgi inntekten</Heading>
            <List>
                <List.Item>skriv inntekten du forventer å få utbetalt før skatt</List.Item>
                <List.Item>alltid i norske kroner</List.Item>
            </List>

            <Link href="/" target="_blank">Slik regner du ut riktig inntekt. (åpnes i ny fane)</Link>{/*todo what is the link?*/}

            <form onSubmit={handleSubmit}>
                <VStack gap="4">
                    <Box borderColor="border-default" borderWidth="1" borderRadius="large" padding="8">
                        <VStack gap="4">
                            <Heading level="2" size="small" spacing>Din inntekt {selectedYear}</Heading>
                            <BodyLong>Du må endre inntektsopplysningene nedenfor hvis de ikke er riktige. Opplysninger som er feil kan gi deg feil utbetaling av uføretrygd. Du kan sende inn ny inntekt så mange ganger du trenger i løpet av året. </BodyLong>
                            { !inntekterResponse.uforeHeleAaret ?
                                <Alert inline variant="info">Du har ikke uføretrygd hele året. Du skal kun legge inn den andre forelderens inntekt for den perioden du har uføretrygd. <Link href="/" target="_blank">Se eksempel.</Link></Alert> : null }
                            {/*todo link? open in new tab?*/}
                            <FormFieldsUser
                                year={selectedYear}
                                errors={brukerErrors}
                                setErrors={setBrukerErrors}
                                setInntekt={(field, belop) => setBrukerinntekt(b => ({...b, [field]:  belop }))}
                                forventedeInntekter={brukerinntekt}
                                inntektSum={getBrukerinntektSum()}
                            />
                        </VStack>
                    </Box>

                    {inntekterResponse?.forventedeInntekter.eps ?
                        <Box borderColor="border-default" borderWidth="1" borderRadius="large" padding="8">
                            <VStack gap="4">
                                <Heading level="2" size="small" spacing>Annen forelders inntekt {selectedYear}</Heading>
                                <BodyLong>Fordi du mottar barnetillegg til uføretrygden, må du også registrere den forventede inntekten til forelderen som du bor sammen med.</BodyLong>
                                <BodyLong><strong>Du skal oppgi inntekten til forelder med fødselsnummer {inntekterResponse.epsPid}</strong></BodyLong>
                                <BodyLong>Du må endre inntektsopplysningene nedenfor hvis de ikke er riktige. Inntekten til den andre forelderen har bare betydning for størrelsen på
                                    barnetillegget ditt. Du kan sende inn ny inntekt så mange ganger du trenger i løpet av året. </BodyLong>

                                { !inntekterResponse.uforeHeleAaret ?
                                    <Alert inline variant="info">Du har ikke uføretrygd hele året. Du skal kun legge inn den andre forelderens inntekt for den perioden du har uføretrygd. <Link href="/" target="_blank">Se eksempel.</Link></Alert> : null }
                                todo link? open in new tab?
                                <FormFieldsEps
                                    year={selectedYear}
                                    errors={epsErrors}
                                    setErrors={setBrukerErrors}
                                    setInntekt={(field, belop) => setAnnenForelderInntekt(b => b ? {...b, [field]: belop} : null)}
                                    forventedeInntekter={annenForelderInntekt || {} as PersonInntekter}
                                    inntektSum={getAnnenForelderInntektSum() || 0}
                                />
                            </VStack>
                        </Box> : null
                    }


                    {sendingErrors.length > 0 ?
                        (<ErrorSummary heading="Du må rette disse feilene før du kan fortsette:">
                        {sendingErrors.map((error) => (<ErrorSummary.Item key={error} href={`#${error}`}>
                            {error}
                        </ErrorSummary.Item>))}
                    </ErrorSummary>) : null}

                    <HStack gap="4">
                        <Button as={RouterLink} to={PageLinks.INDEX} iconPosition="left" icon={<ArrowLeftIcon aria-hidden />} variant="secondary">
                            Tilbake
                        </Button>
                        <Button type="button" variant="primary" iconPosition="right" icon={<ArrowRightIcon aria-hidden />} onClick={handleSubmit} loading={isLoading}>
                            Gå videre og se resultat
                        </Button>
                    </HStack>
                    <HStack>
                        <CancelConfirmationModal/>
                    </HStack>
                </VStack>
            </form>
        </VStack>
    );
};