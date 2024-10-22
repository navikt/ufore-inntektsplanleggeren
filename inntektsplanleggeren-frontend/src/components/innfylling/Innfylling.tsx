import {Box, Button, Heading, HStack, List, VStack} from "@navikt/ds-react";
import React, {useContext, useState} from "react";
import { Link } from "react-router-dom";
import "./innfylling.css"
import {useNavigate} from "react-router-dom";
import {FormStateContext} from "@/context/FormData";
import { FormFields } from "./FormFields";
import { PersonInntekt, submitInntektSimulation} from "@/api/apiFetching";
import {DinInntektTable} from "@/components/innfylling/DinInntektTable";
import {SelectedYearContext} from "@/context/SelectedYear";
import {belopSum, numberFormatWithKr} from "@/common/Utils";
import {basePath} from "@/routes";
import {DataContext} from "@/DataContextProvider";

export const Innfylling = () => {
    const navigate = useNavigate()

    const { brukerinntekt, setBrukerinntekt, annenForelderInntekt, setAnnenForelderInntekt, getBrukerinntektSum, setSimulationInntekt, setFormStep } = useContext(FormStateContext);
    const { inntekterResponse } = useContext(DataContext);
    const { selectedYear } = useContext(SelectedYearContext);
    const isAnnenForelder = true;
    const [errors, setErrors] = useState<Partial<Record<keyof PersonInntekt, string>>>({});

    setFormStep(1)

    const handleSubmit = async (e: { preventDefault: () => void; }) => {
        e.preventDefault();
        try {
            const result = await submitInntektSimulation({ brukerinntekt, annenForelderInntekt }, selectedYear);
            setSimulationInntekt(result.result);
        } catch (error) {
            console.error("Error submitting income simulation:", error);
        }
        navigate(basePath + "/beregning");
    };

    if(inntekterResponse === null) {
        return <div>Loading...</div>
    }

    return (
        <VStack className="form-container">
            <Heading level="2" size="medium">Din inntekt hittil i år</Heading>
            {(inntekterResponse && inntekterResponse.arbeidsinntektOgYtelserHittilIAar) &&
                <DinInntektTable data={inntekterResponse.arbeidsinntektOgYtelserHittilIAar}>
                    Du har mottatt {numberFormatWithKr(belopSum(inntekterResponse.arbeidsinntektOgYtelserHittilIAar))} kr i arbeidsinntekt og pensjonsgivende ytelser hittil i år.
                </DinInntektTable>
            }
            {(inntekterResponse && inntekterResponse.pensjonFraAndreHittilIAar) &&
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
                        <FormFields year={selectedYear} errors={errors} setErrors={setErrors} setInntekt={(field, value) => setBrukerinntekt(b => ({...b, [field]: value}))}
                                    forventedeInntekter={inntekterResponse.forventedeInntekter.bruker} inntektSum={getBrukerinntektSum()} />
                    </Box>

                    {isAnnenForelder ?
                        <Box borderColor="border-default" borderWidth="1" borderRadius="large" padding="8">
                            <Heading level="2" size="small" spacing>Forventede inntekter for annen forelder i ({selectedYear})</Heading>
                            <FormFields year={selectedYear} errors={errors} setErrors={setErrors} setInntekt={(field, value) => setAnnenForelderInntekt(b => ({...b, [field]: value}))}
                                        forventedeInntekter={inntekterResponse.forventedeInntekter.eps} inntektSum={getBrukerinntektSum()} />
                        </Box> : null
                    }

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