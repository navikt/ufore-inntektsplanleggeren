import {Box, Button, Heading, HStack, VStack} from "@navikt/ds-react";
import React, {useContext, useState, useEffect} from "react";
import { Link } from "react-router-dom";
import "./innfylling.css"
import {FormStateContext} from "@/context/FormData";
import { FormFields } from "./FormFields";
import {getInntekter, PersonInntekt} from "@/api/apiFetching";
import {DinInntektTable} from "@/components/innfylling/DinInntektTable";
import {InntekterResponse} from "@/api/model/ApiRequests";
import {SelectedYearContext} from "@/context/SelectedYear";

export const Innfylling = () => {
    const { setPersoninntekt, setAnnenForelderInntekt, personInntektSum, annenForelderInntektSum, formData, setFormStep } = useContext(FormStateContext);
    const { selectedYear } = useContext(SelectedYearContext);
    const isAnnenForelder = true;
    const [errors, setErrors] = useState<Partial<Record<keyof PersonInntekt, string>>>({});
    const [inntektResponse, setInntektResponse] = useState<InntekterResponse>();

    setFormStep(1);

    useEffect(() => {
        getInntekter(selectedYear).then(data => setInntektResponse(data));
    }, [selectedYear]);

    const handleSubmit = (e) => {
        e.preventDefault();
        console.log(formData);

    };

    return (
        <VStack className="form-container">
            <Heading level="2" size="small">Din inntekt hittil i år</Heading>
            {(inntektResponse && inntektResponse.arbeidsinntektOgYtelserHittilIAar) && <DinInntektTable data={inntektResponse.arbeidsinntektOgYtelserHittilIAar}/>}
            {(inntektResponse && inntektResponse.pensjonFraAndreHittilIAar) && <DinInntektTable data={inntektResponse.pensjonFraAndreHittilIAar}/>}

            <form onSubmit={handleSubmit}>
                <VStack gap="4">
                    <Box borderColor="border-default" borderWidth="1" borderRadius="large" padding="8">
                        <Heading level="2" size="small" spacing>Din forventede intekter i ({selectedYear})</Heading>
                        <FormFields year={selectedYear} errors={errors} setErrors={setErrors} setInntekt={setPersoninntekt} data={formData.personInntekt} inntektSum={personInntektSum}/>
                    </Box>

                    {isAnnenForelder && <>
                        <Box borderColor="border-default" borderWidth="1" borderRadius="large" padding="8">
                            <Heading level="2" size="small" spacing>Den forventede Iintekten til annen forelder, Test Testeson, i ({selectedYear})</Heading>
                            <FormFields year={selectedYear} errors={errors} setErrors={setErrors} data={formData.annenForelderInntekt} setInntekt={setAnnenForelderInntekt} inntektSum={annenForelderInntektSum} />
                        </Box>
                    </>}

                    <HStack gap="4">
                        <Button as={Link} to="/" variant="secondary">
                            Tilbake
                        </Button>
                        <Button type="submit" as={Link} to="/beregning" variant="primary">
                            Beregning
                        </Button>
                    </HStack>
                </VStack>
            </form>
        </VStack>
    );
};