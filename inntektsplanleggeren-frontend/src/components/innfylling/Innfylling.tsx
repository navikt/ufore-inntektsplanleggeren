import {BodyLong, Box, Button, Heading, HStack, Loader, VStack} from "@navikt/ds-react";
import React, {FormEvent, MouseEvent, useContext, useEffect, useState} from "react";
import { Link } from "react-router-dom";
import "./innfylling.css"
import {useNavigate} from "react-router-dom";
import {FormStateContext} from "@/context/FormData";
import { FormFields } from "./FormFields";
import {submitInntektSimulation} from "@/api/apiFetching";
import {DinInntektTable} from "@/components/innfylling/DinInntektTable";
import {SelectedYearContext} from "@/context/SelectedYear";
import {belopSum} from "@/common/Utils";
import {DataContext} from "@/DataContextProvider";
import {ForventedeInntekter} from "@/api/model/ApiRequests";
import {FormatKroner} from "@/components/utils/FormatKroner";

export const Innfylling = () => {
    const navigate = useNavigate()
    const { brukerinntekt, setBrukerinntekt, annenForelderInntekt, setAnnenForelderInntekt, getBrukerinntektSum, getAnnenForelderInntektSum, setFormStep } = useContext(FormStateContext);
    const { initialViewData, inntekterResponse, setSimulationResponse } = useContext(DataContext);
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
                <DinInntektTable data={inntekterResponse.arbeidsinntektOgYtelserHittilIAar} type="arbeidsgiver">
                    <BodyLong>Du har mottatt <FormatKroner value={belopSum(inntekterResponse.arbeidsinntektOgYtelserHittilIAar)}/> i arbeidsinntekt og pensjonsgivende ytelser hittil i år.</BodyLong>
                </DinInntektTable>
            }
            {(inntekterResponse.pensjonFraAndreHittilIAar?.length > 0) &&
                <DinInntektTable data={inntekterResponse.pensjonFraAndreHittilIAar} type="pensjonsordning">
                    <BodyLong>Du har mottatt <FormatKroner value={belopSum(inntekterResponse.pensjonFraAndreHittilIAar)}  /> i pensjoner fra andre enn folketrygden hittil i år.</BodyLong>
                </DinInntektTable>
            }

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

                    {(initialViewData?.forventetInntektAnnenForelder !== null) ?
                        <Box borderColor="border-default" borderWidth="1" borderRadius="large" padding="8">
                            <Heading level="2" size="small" spacing>Forventede inntekter for annen forelder i ({selectedYear})</Heading>
                            <FormFields
                                year={selectedYear}
                                errors={errors}
                                setErrors={setErrors}
                                setInntekt={(field, belop) => setAnnenForelderInntekt(b => b ? {...b, [field]: belop} : null)}
                                forventedeInntekter={annenForelderInntekt || {} as ForventedeInntekter}
                                inntektSum={getAnnenForelderInntektSum() || 0}
                            />
                        </Box> : null
                    }

                    <HStack gap="4">
                        <Button as={Link} to="/" variant="secondary">
                            Tilbake
                        </Button>
                        <Button type="submit" variant="primary" onClick={handleSubmit} loading={isLoading}>
                            Oppsummering
                        </Button>
                    </HStack>
                </VStack>
            </form>
        </VStack>
    );
};