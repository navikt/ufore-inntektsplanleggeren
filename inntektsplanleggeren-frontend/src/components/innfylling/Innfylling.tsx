import {Box, Button, Heading, HStack, List, VStack} from "@navikt/ds-react";
import React, {useContext, useState, useEffect} from "react";
import { Link } from "react-router-dom";
import "./innfylling.css"
import {useNavigate} from "react-router-dom";
import {FormStateContext} from "@/context/FormData";
import { FormFields } from "./FormFields";
import {getInntekter, PersonInntekt, submitInntektSimulation} from "@/api/apiFetching";
import {DinInntektTable} from "@/components/innfylling/DinInntektTable";
import {InntekterResponse} from "@/api/model/ApiRequests";
import {SelectedYearContext} from "@/context/SelectedYear";
import {belopSum, numberFormatWithKr} from "@/common/Utils";
import {basePath} from "@/routes";

export const Innfylling = () => {
    const navigate = useNavigate()

    const { setPersoninntekt, setAnnenForelderInntekt, getPersonInntektSum, getAnnenForelderInntektSum,
        setSimulationInntekt, formData, setFormStep } = useContext(FormStateContext);
    const { selectedYear } = useContext(SelectedYearContext);
    const isAnnenForelder = true;
    const [errors, setErrors] = useState<Partial<Record<keyof PersonInntekt, string>>>({});
    const [inntektResponse, setInntektResponse] = useState<InntekterResponse>();

    setFormStep(1)


    useEffect(() => {
        console.log("Fetching inntekt data for year", selectedYear)
        getInntekter(selectedYear).then(data => {
        setInntektResponse(data)});
        console.log(inntektResponse)
    }, [selectedYear]);

    const handleSubmit = async (e: { preventDefault: () => void; }) => {
        e.preventDefault();
        console.log(formData);
        try {
            const result = await submitInntektSimulation(formData, selectedYear);
            setSimulationInntekt(result.result);
        } catch (error) {
            console.error("Error submitting income simulation:", error);
        }
        navigate(basePath + "/beregning");
    };

    return (
        <VStack className="form-container">
            <Heading level="2" size="medium">Din inntekt hittil i år</Heading>
            {(inntektResponse && inntektResponse.arbeidsinntektOgYtelserHittilIAar) &&
                <DinInntektTable data={inntektResponse.arbeidsinntektOgYtelserHittilIAar}>
                    Du har mottatt {numberFormatWithKr(belopSum(inntektResponse.arbeidsinntektOgYtelserHittilIAar))} kr i arbeidsinntekt og pensjonsgivende ytelser hittil i år.
                </DinInntektTable>
            }
            {(inntektResponse && inntektResponse.pensjonFraAndreHittilIAar) &&
                <DinInntektTable data={inntektResponse.pensjonFraAndreHittilIAar}>
                    Du har mottatt {numberFormatWithKr(belopSum(inntektResponse.pensjonFraAndreHittilIAar))} kr i pensjoner fra andre enn folketrygden hittil i år.
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
                        <Heading level="2" size="small" spacing>Din forventede intekter i ({selectedYear})</Heading>
                        <FormFields year={selectedYear} errors={errors} setErrors={setErrors} setInntekt={setPersoninntekt} data={formData.personInntekt}
                                    forventedeInntekter={inntektResponse?.forventedeInntekter.bruker ?? null} inntektSum={getPersonInntektSum}/>
                    </Box>

                    {isAnnenForelder && <>
                        <Box borderColor="border-default" borderWidth="1" borderRadius="large" padding="8">
                            <Heading level="2" size="small" spacing>Den forventede Iintekten til annen forelder, Test Testeson, i ({selectedYear})</Heading>
                            <FormFields year={selectedYear} errors={errors} setErrors={setErrors} data={formData.annenForelderInntekt}
                                        setInntekt={setAnnenForelderInntekt} inntektSum={getAnnenForelderInntektSum} forventedeInntekter={inntektResponse?.forventedeInntekter.eps ?? null}/>
                        </Box>
                    </>}

                    <HStack gap="4">
                        <Button as={Link} to={basePath} variant="secondary">
                            Tilbake
                        </Button>
                        <Button type="submit" as={Link} to="/beregning" variant="primary" onClick={handleSubmit}>
                            Beregning
                        </Button>
                    </HStack>
                </VStack>
            </form>
        </VStack>
    );
};