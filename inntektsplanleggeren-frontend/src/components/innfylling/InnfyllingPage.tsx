import {BodyLong, Box, Button, Heading, HStack, List, Loader, VStack, Alert, Link} from "@navikt/ds-react";
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
import {PersonInntekter} from "@/api/model/ApiRequests";
import {FormatKroner} from "@/components/utils/FormatKroner";
import {ArrowLeftIcon, ArrowRightIcon} from "@navikt/aksel-icons";
import {PageLinks} from "@/FormContainer";
import {FormFieldsEps} from "@/components/innfylling/FormFieldsEps";
import {CancelConfirmationModal} from "@/components/common/CancelConfirmationModal";


export const InnfyllingPage = () => {
    const navigate = useNavigate()
    const { brukerinntekt, setBrukerinntekt, annenForelderInntekt, setAnnenForelderInntekt, getBrukerinntektSum, getAnnenForelderInntektSum, setFormStep } = useContext(FormStateContext);
    const { inntekterResponse, setSimulationResponse } = useContext(DataContext);
    const { selectedYear } = useContext(SelectedYearContext);
    const [errors, setErrors] = useState<Partial<Record<keyof PersonInntekter, string>>>({});
    const [isLoading, setIsLoading] = useState<boolean>(false);

    useEffect(() => {
        setFormStep(1);
    }, [setFormStep]);


    const handleSubmit = async (e: MouseEvent | FormEvent) => {
        e.preventDefault();

        try {
            setIsLoading(true);
            const result = await simulate(brukerinntekt, annenForelderInntekt, selectedYear);
            setSimulationResponse(result);
            navigate(PageLinks.BEREGNING);
        } catch (error) {
            console.error("Error submitting income simulation:", error);
        }

        navigate(PageLinks.BEREGNING);
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
                                errors={errors}
                                setErrors={setErrors}
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
                                    errors={errors}
                                    setErrors={setErrors}
                                    setInntekt={(field, belop) => setAnnenForelderInntekt(b => b ? {...b, [field]: belop} : null)}
                                    forventedeInntekter={annenForelderInntekt || {} as PersonInntekter}
                                    inntektSum={getAnnenForelderInntektSum() || 0}
                                />
                            </VStack>
                        </Box> : null
                    }

                    <HStack gap="4">
                        <Button as={RouterLink} to={PageLinks.INDEX} iconPosition="left" icon={<ArrowLeftIcon aria-hidden />} variant="secondary">
                            Tilbake
                        </Button>
                        <Button type="submit" variant="primary" iconPosition="right" icon={<ArrowRightIcon aria-hidden />} onClick={handleSubmit} loading={isLoading}>
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