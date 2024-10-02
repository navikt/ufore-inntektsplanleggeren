import {Button, Heading, HStack, VStack} from "@navikt/ds-react";
import React, {useContext, useState, useEffect} from "react";
import {Link, useNavigate, useSearchParams} from "react-router-dom";
import "./innfylling.css"
import {FormStateContext} from "@/context/FormData";
import { FormFields } from "./FormFields";
import {getInntekter, PersonInntekt} from "@/api/apiFetching";
import {DinInntektTable} from "@/components/innfylling/DinInntektTable";
import {InntektDetaljer, InntekterResponse} from "@/api/model/ApiRequests";

export const Innfylling = () => {
    const [ searchParams ] = useSearchParams();
    const navigate = useNavigate();
    const year = searchParams.get('year');

    const { selectedYear, setSelectedYear, setPersoninntekt, setAnnenForelderInntekt, personInntektSum, annenForelderInntektSum, formData, setFormStep } = useContext(FormStateContext);
    const isAnnenForelder = true;
    const [errors, setErrors] = useState<Partial<Record<keyof PersonInntekt, string>>>({});
    const [inntektData, setInntektData] = useState<InntekterResponse>();

    setFormStep(1);

    useEffect(() => {
        if (year) {
            setSelectedYear(year);
            getInntekter(year).then(data => setInntektData(data));
        }
    }, [year, setSelectedYear]);

    const handleSubmit = (e) => {
        e.preventDefault();
        console.log(formData);
    };

    if (!year) {
        navigate("/");
    }

    return (
        <VStack gap="4">
            <Heading level="2" size="small">Din inntekt hittil i år</Heading>
            {(inntektData && inntektData.arbeidsinntektOgYtelserHittilIAar) && <DinInntektTable data={inntektData.arbeidsinntektOgYtelserHittilIAar}/>}
            {(inntektData && inntektData.pensjonFraAndreHittilIAar) && <DinInntektTable data={inntektData.pensjonFraAndreHittilIAar}/>}

            <Heading level="2" size="small">Din forventede intekter i ({selectedYear})</Heading>

            <form onSubmit={handleSubmit}>
                <HStack>
                    <FormFields year={selectedYear} errors={errors} setErrors={setErrors} setInntekt={setPersoninntekt} data={formData.personInntekt} inntektSum={personInntektSum}/>
                </HStack>

                {isAnnenForelder && <>
                    <Heading level="2" size="small">Den forventede Iintekten til annen forelder, Test Testeson, i ({selectedYear})</Heading>
                    <HStack>
                        <FormFields year={selectedYear} errors={errors} setErrors={setErrors} data={formData.annenForelderInntekt} setInntekt={setAnnenForelderInntekt} inntektSum={annenForelderInntektSum} />
                    </HStack>
                </>}

                <HStack>
                    <Button as={Link} to="/" variant="secondary">
                        Tilbake
                    </Button>
                    <Button as={Link} to="/beregning" variant="secondary">
                        Beregning
                    </Button>
                </HStack>
            </form>
        </VStack>
    );
};