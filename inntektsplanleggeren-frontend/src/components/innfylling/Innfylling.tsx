import {Box, Button, Heading, HStack, VStack} from "@navikt/ds-react";
import React, {useContext, useState, useEffect} from "react";
import { Link } from "react-router-dom";
import "./innfylling.css"
import {useNavigate} from "react-router-dom";
import {FormStateContext} from "@/context/FormData";
import { FormFields } from "./FormFields";
import {getInntekter, PersonInntekt, submitInntektSimulation} from "@/api/apiFetching";
import {DinInntektTable} from "@/components/innfylling/DinInntektTable";
import {InntekterResponse, SubmitInntektSimulationResponse} from "@/api/model/ApiRequests";
import {SelectedYearContext} from "@/context/SelectedYear";
import {belopSum, numberFormatWithKr} from "@/common/Utils";

export const Innfylling = () => {
    const navigate = useNavigate()

    const { setPersoninntekt, setAnnenForelderInntekt, getPersonInntektSum, getAnnenForelderInntektSum,
        setSimulationInntekt, formData, setFormStep } = useContext(FormStateContext);
    const { selectedYear } = useContext(SelectedYearContext);
    const isAnnenForelder = true;
    const [errors, setErrors] = useState<Partial<Record<keyof PersonInntekt, string>>>({});
    const [inntektResponse, setInntektResponse] = useState<InntekterResponse>();

    setFormStep(1);

    useEffect(() => {
        getInntekter(selectedYear).then(data => setInntektResponse(data));
    }, [selectedYear]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        console.log(formData);
        try {
            const result = await submitInntektSimulation(formData, selectedYear);
            setSimulationInntekt(result.result);
        } catch (error) {
            console.error("Error submitting income simulation:", error);
        }
        navigate("/beregning");
    };

    return (
        <VStack className="form-container">
            <Heading level="2" size="small">Din inntekt hittil i år</Heading>
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

            <form onSubmit={handleSubmit}>
                <VStack gap="4">
                    <Box borderColor="border-default" borderWidth="1" borderRadius="large" padding="8">
                        <Heading level="2" size="small" spacing>Din forventede intekter i ({selectedYear})</Heading>
                        <FormFields year={selectedYear} errors={errors} setErrors={setErrors} setInntekt={setPersoninntekt} data={formData.personInntekt} inntektSum={getPersonInntektSum}/>
                    </Box>

                    {isAnnenForelder && <>
                        <Box borderColor="border-default" borderWidth="1" borderRadius="large" padding="8">
                            <Heading level="2" size="small" spacing>Den forventede Iintekten til annen forelder, Test Testeson, i ({selectedYear})</Heading>
                            <FormFields year={selectedYear} errors={errors} setErrors={setErrors} data={formData.annenForelderInntekt} setInntekt={setAnnenForelderInntekt} inntektSum={getAnnenForelderInntektSum} />
                        </Box>
                    </>}

                    <HStack gap="4">
                        <Button as={Link} to="/" variant="secondary">
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