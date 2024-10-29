import {Box, Button, Heading, HStack, List, Loader, VStack} from "@navikt/ds-react";
import React, {FormEvent, MouseEvent, useContext, useEffect, useState} from "react";
import { Link } from "react-router-dom";
import "./innfylling.css"
import {useNavigate} from "react-router-dom";
import {FormStateContext} from "@/context/FormData";
import { FormFields } from "./FormFields";
import {submitInntektSimulation} from "@/api/apiFetching";
import {DinInntektTable} from "@/components/innfylling/DinInntektTable";
import {SelectedYearContext} from "@/context/SelectedYear";
import {belopSum, numberFormatWithKr} from "@/common/Utils";
import {DataContext} from "@/DataContextProvider";
import {ForventedeInntekter} from "@/api/model/ApiRequests";

export const Innfylling = () => {
    const navigate = useNavigate()
    const { brukerinntekt, setBrukerinntekt, annenForelderInntekt, getBrukerinntektSum, setFormStep } = useContext(FormStateContext);
    const { inntekterResponse, setSimulationResponse } = useContext(DataContext);
    const { selectedYear } = useContext(SelectedYearContext);
    const [errors, setErrors] = useState<Partial<Record<keyof ForventedeInntekter, string>>>({});
    const [isLoading, setIsLoading] = useState<boolean>(false)

    useEffect(() => {
        setFormStep(1);
    }, [setFormStep]);

    const handleSubmit = async (e: MouseEvent | FormEvent) => {
        e.preventDefault();

        try {
            setIsLoading(true);
            const result = await submitInntektSimulation(brukerinntekt, annenForelderInntekt, selectedYear);
            setSimulationResponse(result);
            navigate("/oppsummering");
        } catch (error) {
            console.error("Error submitting income simulation:", error);
        }

        navigate("/oppsummering");
    };

    if(inntekterResponse === null) {
        return <Loader />;
    }

    return (
        <VStack className="form-container">
            <Heading level="2" size="medium">Din inntekt hittil i år</Heading>
            {(inntekterResponse.arbeidsinntektOgYtelserHittilIAar?.length > 0) &&
                <DinInntektTable data={inntekterResponse.arbeidsinntektOgYtelserHittilIAar}>
                    Du har mottatt {numberFormatWithKr(belopSum(inntekterResponse.arbeidsinntektOgYtelserHittilIAar))} kr i arbeidsinntekt og pensjonsgivende ytelser hittil i år.
                </DinInntektTable>
            }
            {(inntekterResponse.pensjonFraAndreHittilIAar?.length > 0) &&
                <DinInntektTable data={inntekterResponse.pensjonFraAndreHittilIAar}>
                    Du har mottatt {numberFormatWithKr(belopSum(inntekterResponse.pensjonFraAndreHittilIAar))} kr i pensjoner fra andre enn folketrygden hittil i år.
                </DinInntektTable>
            }

            <section>
                <VStack gap="4">
                <Heading level="2" size="medium">Oppgi forventede inntekter</Heading>
                    <List title="Slik skal du oppgi inntekten">
                        <List.Item>du skal kun oppgi inntekt for den perioden av året som du mottar uføretrygd
                            før skatt </List.Item>
                        <List.Item>det du tror du kommer til å ha tjent når året er slutt / årlig beløp</List.Item>
                        <List.Item>alltid i norske kroner</List.Item>
                    </List>
                </VStack>
            </section>

            <form onSubmit={handleSubmit}>
                <VStack gap="4">
                    <Box borderColor="border-default" borderWidth="1" borderRadius="large" padding="8">
                        <Heading level="2" size="small" spacing>Din forventede inntekter i ({selectedYear})</Heading>
                        <FormFields
                            year={selectedYear}
                            errors={errors}
                            setErrors={setErrors}
                            setInntekt={(field, belop) => setBrukerinntekt(b => ({...b, [field]:  belop }))}
                            forventedeInntekter={brukerinntekt}
                            inntektSum={getBrukerinntektSum()}
                        />
                    </Box>

                    {/*{(initialViewData?.hasBarneTilleggFellesbarn || annenForelderInntekt != null) ?*/}
                    {/*    <Box borderColor="border-default" borderWidth="1" borderRadius="large" padding="8">*/}
                    {/*        <Heading level="2" size="small" spacing>Forventede inntekter for annen forelder i ({selectedYear})</Heading>*/}
                    {/*        <FormFields*/}
                    {/*            year={selectedYear}*/}
                    {/*            errors={errors}*/}
                    {/*            setErrors={setErrors}*/}
                    {/*            setInntekt={(field, belop) => setAnnenForelderInntekt(b => ({...b, [field]:  belop}))}*/}
                    {/*            forventedeInntekter={annenForelderInntekt}*/}
                    {/*            inntektSum={getAnnenForelderInntektSum()}*/}
                    {/*        />*/}
                    {/*    </Box> : null*/}
                    {/*}*/}

                    <HStack gap="4">
                        <Button as={Link} to="/" variant="secondary">
                            Tilbake
                        </Button>
                        <Button type="submit" variant="primary" onClick={handleSubmit} loading={isLoading}>
                            Beregning
                        </Button>
                    </HStack>
                </VStack>
            </form>
        </VStack>
    );
};