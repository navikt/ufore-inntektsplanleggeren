import {Button, Heading, HStack, VStack} from "@navikt/ds-react";
import React, {useContext, useState} from "react";
import {Link, useNavigate, useSearchParams} from "react-router-dom";
import "./innfylling.css"
import {FormStateContext} from "@/context/FormData";
import { FormFields } from "./FormFields";
import {PersonInntekt} from "@/api/apiFetching";
import {DinInntektTable} from "@/components/innfylling/DinInntektTable";
import {InntektDetaljer} from "@/api/model/ApiRequests";



export const Innfylling = () => {
    const [ searchParams ] = useSearchParams();
    const navigate = useNavigate()
    const year = searchParams.get('year')

    const { selectedYear, setSelectedYear, setPersoninntekt, setAnnenForelderInntekt, personInntektSum, annenForelderInntektSum, formData, setFormStep } = useContext(FormStateContext);
    const isAnnenForelder = true;
    const [errors, setErrors] = useState<Partial<Record<keyof PersonInntekt, string>>>({})
    setFormStep(1)

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
        <VStack gap="4">
            <Heading level="2" size="small">Din inntekt hittil i år</Heading>
            <DinInntektTable data={data}/>

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
                <Button as={Link} to="/beregning" variant="secondary" >
                    Beregning
                </Button>
                {/*<Button type="submit">*/}
                {/*    Beregning*/}
                {/*</Button>*/}
            </HStack>
            </form>
        </VStack>
    );
};

const data = [
    {
        "maned": 5,
        "belop": 53426.0,
        "inntektsgivere": [
        "Veterinær AS",
        "Grønnsakssuppekjøkkenet AS"
    ]
    },
    {
        "maned": 6,
        "belop": 34543.0,
        "inntektsgivere": [
        "Isbilen AS"
    ]
    },
    {
        "maned": 7,
        "belop": 54001.0,
        "inntektsgivere": [
        "Veterinær AS"
    ]
    },
    {
        "maned": 8,
        "belop": 7641.0,
        "inntektsgivere": [
        "Veterinær AS"
    ]
    }
    ];