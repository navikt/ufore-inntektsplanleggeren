import {Box, Button, Heading, HStack} from "@navikt/ds-react";
import React, {useContext, useState} from "react";
import {Link, useNavigate, useParams, useSearchParams} from "react-router-dom";
import "./innfylling.css"
import {FormStateContext} from "@/SelectedYear/FormData";
import { FormFields } from "./FormFields";
import {InntektInnfylling, PersonInntekt, sendInntektsdata} from "@/api/apiFetching";
import {DinInntektTable} from "@/components/innfylling/DinInntektTable";



export const Innfylling = () => {
    const [ searchParams ] = useSearchParams();
    const navigate = useNavigate()
    const year = searchParams.get('year')

    const { selectedYear, setFormData, setSelectedYear, setPersoninntekt, setAnnenForelderInntekt, personInntektSum, annenForelderInntektSum, formData } = useContext(FormStateContext);
    const isAnnenForelder = true;
    const [errors, setErrors] = useState<Partial<Record<keyof PersonInntekt, string>>>({})

    React.useEffect(() => {

        if (year) {
            setSelectedYear(year)
        }
        }, [searchParams, setSelectedYear]);


    // make this function call the api
    const handleSubmit = (e) => {
        e.preventDefault();
        console.log(formData)
    };

    if (!year) {
        navigate("/")
    }

    return (
        <>
            <Heading level="2" size="small">Din inntekt hittil i år</Heading>
            <DinInntektTable/>

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
                </>
            }


            <HStack>
                <Button as={Link} to="/" variant="secondary" >
                    Tilbake
                </Button>
                <Button type="submit">
                    Beregning
                </Button>
            </HStack>
            </form>
        </>
    );
};