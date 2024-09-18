import {Box, Button, Heading, HStack} from "@navikt/ds-react";
import React, {useContext, useState} from "react";
import {Link} from "react-router-dom";
import "./innfylling.css"
import {FormStateContext} from "@/SelectedYear/SelectedYear";
import { FormFields } from "./FormFields";
import {InntektInnfylling, PersonInntekt} from "@/api/apiFetching";



export const Innfylling = () => {
    const { selectedYear } = useContext(FormStateContext);
    const isAnnenForelder = true;
    const [errors, setErrors] = useState<Partial<Record<keyof PersonInntekt, string>>>({})
    const [inntektInnfylling, setInntektInnfylling] = useState<InntektInnfylling> ({
        personInntekt: {
            arbeidsinntekt: 0,
            navYtelse: 0,
            naeringsinntekt: 0,
            inntektFraUtlandet: 0,
            pensjonFraAndre: 0,
            pensjonFraUtlandet: 0
        },
        annenForelderInntekt: {
            arbeidsinntekt: 0,
            navYtelse: 0,
            naeringsinntekt: 0,
            inntektFraUtlandet: 0,
            pensjonFraAndre: 0,
            pensjonFraUtlandet: 0
        }
    })

    return (
        <>
            <Heading level="2" size="small">Din forventede intekter i ({selectedYear})</Heading>
            <HStack>
                <FormFields year={selectedYear} errors={errors} setErrors={setErrors} personInntekt={inntektInnfylling.personInntekt}/>
            </HStack>

            {isAnnenForelder ? <>
                <Heading level="2" size="small">Den forventede Iintekten til annen forelder, Test Testeson, i ({selectedYear})</Heading>
                <HStack>
                    <FormFields year={selectedYear} errors={errors} setErrors={setErrors} personInntekt={inntektInnfylling.annenForelderInntekt} />
                </HStack>
                </>: <> </>
            }


            <HStack>
                <Button as={Link} to="/" variant="secondary">
                    Tilbake
                </Button>
                <Button as={Link} to="/beregning" variant="primary" >
                    Beregning
                </Button>
            </HStack>
        </>
    );
};