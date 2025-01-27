import {
    Alert,
    BodyLong,
    Box,
    Button,
    ErrorSummary,
    Heading,
    HStack,
    Link,
    List,
    Loader,
    VStack
} from "@navikt/ds-react";
import React, {FormEvent, MouseEvent, useContext, useEffect, useState} from "react";
import "./innfylling.css"
import {Link as RouterLink, useNavigate} from "react-router-dom";
import {FormStateContext} from "@/context/FormData";
import {FormFieldsUser} from "./FormFieldsUser";
import {simulate} from "@/api/apiFetching";
import {DinInntektTable} from "@/components/common/DinInntektTable";
import {belopSum} from "@/common/Utils";
import {DataContext} from "@/DataContextProvider";
import {PersonInntekter, SimulationResponse} from "@/api/model/ApiRequests";
import {FormatKroner} from "@/components/utils/FormatKroner";
import {ArrowLeftIcon, ArrowRightIcon} from "@navikt/aksel-icons";
import {getFullPathForPage, PageLinks} from "@/FormContainer";
import {FormFieldsEps} from "@/components/innfylling/FormFieldsEps";
import {CancelConfirmationModal} from "@/components/common/CancelConfirmationModal";
import {MessageCodes} from "@/api/model/MessageCodes";
import {ErrorCode, ErrorResponse} from "@/components/common/Error";


export const InnfyllingPage = () => {
    const navigate = useNavigate()
    const { selectedYear, previousYear, brukerinntekt, setBrukerinntekt, annenForelderInntekt, setAnnenForelderInntekt, getBrukerinntektSum, getAnnenForelderInntektSum, setFormStep } = useContext(FormStateContext);
    const { inntekterResponse, setSimulationResponse, setErrorMessage} = useContext(DataContext);
    const [brukerErrors, setBrukerErrors] = useState<Partial<Record<keyof PersonInntekter, string>>>({});
    const [epsErrors, setEpsErrors] = useState<Partial<Record<keyof PersonInntekter, string>>>({});
    const [isLoading, setIsLoading] = useState<boolean>(false);

    useEffect(() => {
        setFormStep(1);
    }, [setFormStep]);

    const checkForFieldErrors = () : boolean => {
        const errorMessages = Object.values(brukerErrors).concat(Object.values(epsErrors)).filter((message) => message !== undefined)
        return errorMessages.length > 0
    }

    const checkForSendingErrors = (response: SimulationResponse) : boolean => {
        const bErrors: Partial<Record<keyof PersonInntekter, string>> = {};
        const eErrors: Partial<Record<keyof PersonInntekter, string>> = {};
        let isError = false

        //todo review texts below
        for (const message of response.messages) {
            if (message.messageCode === MessageCodes.ARBEIDSINNTEKT_GIVEN_SMALLER_THAN_HITTIL_I_AAR) {
                isError = true
                if(message.metadata["AFFECTED_FIELD"] === "ARBEIDSINNTEKT_BRUKER") {
                    bErrors["arbeidsinntekt"] = `Beløpet kan ikke være mindre enn ${message.metadata["SUM_HITTIL_I_AAR"]}, fordi du allerede har fått dette i lønn og pengestøtte`
                } else if(message.metadata["AFFECTED_FIELD"] === "ARBEIDSINNTEKT_EPS") {
                    eErrors["arbeidsinntekt"] = `Beløpet må være høyere enn det den andre forelderen har fått i lønn og pengestøtte hittil i år. Den andre forelderen kan se inntekter som er registrert hittil i år hos Skatteetaten.`;
                }
            }
            else if (message.messageCode === MessageCodes.ANDRE_YTELSER_SMALLER_THAN_HITTIL_I_AAR) {
                isError = true
                if(message.metadata["AFFECTED_FIELD"] === "ANDRE_YTELSER_BRUKER") {
                    bErrors["andrePensjonsgivendeYtelser"] = `Beløpet kan ikke være mindre enn ${message.metadata["SUM_HITTIL_I_AAR"]}, fordi du allerede har fått dette i pensjoner fra andre enn folketrygden hittil i år.`;
                } else if(message.metadata["AFFECTED_FIELD"] === "ANDRE_YTELSER_EPS") {
                    eErrors["andrePensjonsgivendeYtelser"] = `Beløpet må være høyere enn det den andre forelderen har fått i pensjoner hittil i år. Den andre forelderen kan se inntekter som er registrert hittil i år hos Skatteetaten.`;
                }
            }
        }

        setBrukerErrors(bErrors);
        setEpsErrors(eErrors);
        return isError
    }


    const handleSubmit = async (e: MouseEvent | FormEvent) => {
        e.preventDefault();

        try {
            setIsLoading(true);
            if(checkForFieldErrors()){
                setIsLoading(false);
                return
            }
            const result = await simulate(brukerinntekt, annenForelderInntekt, selectedYear);
            if (result instanceof ErrorResponse){
                setErrorMessage(result.message)
                setIsLoading(false)
            } else {
                if (checkForSendingErrors(result)) {
                    setIsLoading(false);
                } else {
                    setIsLoading(false);
                    setSimulationResponse(result);
                    navigate(getFullPathForPage(PageLinks.BEREGNING));
                }
            }
        } catch (error) {
            setIsLoading(false);
            setErrorMessage(ErrorCode.GENERIC_ERROR)
        }
    };

    if(inntekterResponse === null) {
        return <Loader/>;
    }

    const errorSummary = (errors: Partial<Record<keyof PersonInntekter, string>>, suffix: string) => {
        return (
            <>
            {errors.arbeidsinntekt && <ErrorSummary.Item key={'arbeidsinntekt_'+suffix} href={'#arbeidsinntekt_'+suffix}>
                {errors.arbeidsinntekt}
            </ErrorSummary.Item>}
            {errors.naeringsinntekt && <ErrorSummary.Item key={'naeringsinntekt_'+suffix} href={'#naeringsinntekt_'+suffix}>
                {errors.naeringsinntekt}
            </ErrorSummary.Item>}
            {errors.inntektUtland && <ErrorSummary.Item key={'inntektUtland_'+suffix} href={'#inntektUtland_'+suffix}>
                {errors.inntektUtland}
            </ErrorSummary.Item>}
            {errors.pensjonUtland && <ErrorSummary.Item key={'pensjonUtland_'+suffix} href={'#pensjonUtland_'+suffix}>
                {errors.pensjonUtland}
            </ErrorSummary.Item>}
            {errors.andrePensjonsgivendeYtelser && <ErrorSummary.Item key={'andrePensjonsgivendeYtelser_'+suffix} href={'#andrePensjonsgivendeYtelser_'+suffix}>
                {errors.andrePensjonsgivendeYtelser}
            </ErrorSummary.Item>}
            </>
        )
    }


    return (
        <VStack className="form-container">
            {(inntekterResponse.pensjonFraAndreHittilIAar?.length > 0 || inntekterResponse.pensjonFraAndreHittilIAar?.length > 0) &&
                <VStack>
                    <Heading level="2" size="medium">Din inntekt hittil i år</Heading>
                    <BodyLong> Under kan du se hvilke inntekter som er registrert via A-meldingen. Det er likevel viktig at du sender inn forventet inntekt for hele året til oss.
                        Når vi får registrert riktig inntekt, kan vi gjøre en riktig beregning av din utbetaling.</BodyLong>
                </VStack>}

            {(inntekterResponse.arbeidsinntektOgYtelserHittilIAar?.length > 0) &&
                <DinInntektTable data={inntekterResponse.arbeidsinntektOgYtelserHittilIAar} type="arbeidsgiver">
                    <Heading size={"xsmall"}>Arbeidsinntekt og pengestøtter</Heading>
                    <BodyLong>Vi har registrert at du har fått <strong><FormatKroner value={belopSum(inntekterResponse.arbeidsinntektOgYtelserHittilIAar)}/></strong> i arbeidsinntekt og pengestøtter hittil i år.</BodyLong>
                </DinInntektTable>
            }
            {(inntekterResponse.pensjonFraAndreHittilIAar?.length > 0) &&
                <DinInntektTable data={inntekterResponse.pensjonFraAndreHittilIAar} type="pensjonsordning">
                    <Heading size={"xsmall"}>Pensjoner fra andre enn folketrygden</Heading>
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
                                <Alert inline variant="info">Du har ikke uføretrygd hele året. Du skal kun legge inn den andre forelderens inntekt for den perioden du har uføretrygd.</Alert> : null }
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
                                    setErrors={setEpsErrors}
                                    setInntekt={(field, belop) => setAnnenForelderInntekt(b => b ? {...b, [field]: belop} : null)}
                                    forventedeInntekter={annenForelderInntekt || {} as PersonInntekter}
                                    inntektSum={getAnnenForelderInntektSum() || 0}
                                />
                            </VStack>
                        </Box> : null
                    }


                    {checkForFieldErrors() ?
                        (<ErrorSummary heading="Du må rette disse feilene før du kan fortsette:">
                            {errorSummary(brukerErrors, "bruker")}
                            {errorSummary(epsErrors, "eps")}
                    </ErrorSummary>) : null}

                    <HStack gap="4">
                        <Button as={RouterLink} to={previousYear != null ? PageLinks.FORRIGE_INNTEKTER : PageLinks.INDEX} iconPosition="left" icon={<ArrowLeftIcon aria-hidden />} variant="secondary">
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