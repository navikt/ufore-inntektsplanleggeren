import {Box, Button, ErrorSummary, ExpansionCard, Heading, HStack, ReadMore, TextField, VStack} from "@navikt/ds-react";
import React, {useContext, useState} from "react";
import {Link} from "react-router-dom";
import "./innfylling.css"
import {FormStateContext} from "@/SelectedYear/SelectedYear";

interface IFormData {
    arbeidsinntekt: number
    navYtelse: number
    naeringsinntekt: number
    inntektFraUtlandet: number
    pensjonFraAndre: number
    pensjonFraUtlandet: number
}

function annenForelderInnfylling() {
    return (
        <>
            <VStack gap={"5 5"}>
                <TextField label="Arbeidsinntekt fra arbeidsgiver" inputMode="numeric" id="annenForelderAbeidsinntekt"/>
                <div className="description-card">
                    <ReadMore header="Disse pensjonsgivende ytelsene skal med">
                        description
                    </ReadMore>
                </div>
            </VStack>

            <VStack>
                <TextField label="Pensjonsgivende ytelser fra oss/NAV" inputMode="numeric" id="annenForelderNavYtelse"
                           description="Uføretrygden skal ikke tas med"/>
                <ReadMore header="Disse pensjonsgivende ytelsene skal med">
                    description
                </ReadMore>
            </VStack>

            <VStack>
                <TextField label="Næringsinntekt" inputMode="numeric" id="annenForelderMaeringsinntekt"/>
                <ReadMore header="Tekst tekst tekst">
                    description
                </ReadMore>
            </VStack>

            <VStack>
                <TextField label="Inntekt fra utlandet, i norske kroner" inputMode="numeric" id={"inntektFraUtlandet"}/>
                <ReadMore header="Tekst tekst tekst">
                    description
                </ReadMore>
            </VStack>

            <VStack>
                <TextField label="Pensjoner og uførepensjon fra andre enn folketrygden (ikke AFP)" inputMode="numeric" id={"pensjonFraAndre"}/>
                <ReadMore header="Dette skal du oppgi her">
                    description
                </ReadMore>
            </VStack>

            <VStack>
                <TextField label="Pensjoner fra utlandet, i norske kroner" inputMode="numeric" id={"pensjonFraUtlandet"}/>
                <ReadMore header="Tekst tekst tekst">
                    description
                </ReadMore>
            </VStack>
        </>
    )
}

export const Innfylling = () => {
    const { selectedYear } = useContext(FormStateContext);

    const [errors, setErrors] = useState<Partial<Record<keyof IFormData, string>>>({})
    const [inntektSum, setInntektSum] = useState<number>(0)

    function updateInntektSum(formData: IFormData) {
        const sum = Object.values(formData).reduce((acc, value) => acc + parseInt(value), 0)
        setInntektSum(sum)
    }



    const handleSubmit:  React.FormEventHandler<HTMLFormElement> = (e) =>{
        e.preventDefault()
        const formData: IFormData = {
            arbeidsinntekt: e.currentTarget.arbeidsinntekt.value,
            navYtelse: e.currentTarget.navYtelse.value,
            naeringsinntekt: e.currentTarget.naeringsinntekt.value,
            inntektFraUtlandet: e.currentTarget.inntektFraUtlandet.value,
            pensjonFraAndre: e.currentTarget.pensjonFraAndre.value,
            pensjonFraUtlandet: e.currentTarget.pensjonFraUtlandet.value
        }

        const newErrors = Object.entries(formData).reduce((acc, [key, value]) => {
            if (value === '') {
                return {
                    ...acc,
                    [key]: 'Feltet er påkrevd'
                }
            } else if(isNaN(parseInt(value))) {
                return {
                    ...acc,
                    [key]: 'Maa vaere tall'
                }
            } else if(value < 0) {
                return {
                    ...acc,
                    [key]: 'Må ikke være mindre enn 0'
                }
            }
            return {}
        }, {})

        setErrors(newErrors)

        if (Object.keys(newErrors).length === 0) {
            updateInntektSum(formData)
        }
    }

    console.log(errors)
    return (
        <>

            <Heading level="2" size="small">Din forventede intekter i ({selectedYear})</Heading>
            {Object.keys(errors).length > 0 && <ErrorSummary heading="Du må rette disse feilene før du kan sende inn søknaden:">
                {Object.entries(errors).map(([key, value]) =>
                    <ErrorSummary.Item href={`#${key}`}><>{value}</></ErrorSummary.Item>
                )}
            </ErrorSummary>}

            <HStack>
                <form onSubmit={handleSubmit}>
                    <VStack gap={"5 5"}>
                        <TextField label="Arbeidsinntekt fra arbeidsgiver" inputMode="numeric" id="arbeidsinntekt"/>
                        <div className="description-card">
                            <ReadMore header="Denne arbeidsinnteken skal med ">
                                Legg inn lønn fra arbeidsgiver som et årsbeløp før skatt. Ta med eventuell bonus og overtidsbetaling og feriepenger som blir utbetalt i {selectedYear}.
                            </ReadMore>
                        </div>
                    </VStack>

                    <VStack>
                        <TextField label="Pensjonsgivende ytelser fra oss/NAV" inputMode="numeric" id="navYtelse"
                                   description="Uføretrygden skal ikke tas med"/>
                        <ReadMore header="Disse pensjonsgivende ytelsene skal med">
                            Har du sykepenger, arbeidsavklaringspenger, dagpenger, foreldrepenger, svangerskapspenger, omstillingsstønad, overgangsstønad, ventelønn, omsorgs-, pleie- eller opplæringspenger
                            fra oss, skal du oppgi dette her. Uføretrygden skal ikke tas med.
                        </ReadMore>
                    </VStack>

                    <VStack>
                        <TextField label="Næringsinntekt" inputMode="numeric" id="naeringsinntekt"/>
                        <ReadMore header="Tekst tekst tekst">
                            Legg inn det du forventer å tjene fra næringsvirksomhet i Norge som et årsbeløp før skatt.
                        </ReadMore>
                    </VStack>

                    <VStack>
                        <TextField label="Inntekt fra utlandet, i norske kroner" inputMode="numeric" id="inntektFraUtlandet"/>
                        <ReadMore header="Tekst tekst tekst">
                            Legg inn det du forventer å tjene i arbeidsinntekt og næringsinntekt fra utlandet som et årsbeløp før skatt.
                        </ReadMore>
                    </VStack>

                    <VStack>
                        <TextField label="Pensjoner og uførepensjon fra andre enn folketrygden" inputMode="numeric" id="pensjonFraAndre"/>
                        <ReadMore header="Dette skal du oppgi her">
                            Legg inn pensjoner fra andre enn oss som et årsbeløp før skatt. Oppgi pensjoner fra både offentlige og private ordninger. Dette inkluderer også uførepensjon fra andre enn oss. Har du krigspensjon eller familiepleieytelse
                            fra oss, skal du oppgi dette også her. Ikke oppgi eventuell alderspensjon du mottar fra oss. Inntekten du oppgir her har bare betydning for størrelsen på barnetillegget ditt.
                        </ReadMore>
                    </VStack>

                    <VStack>
                        <TextField label="Pensjoner fra utlandet, i norske kroner" inputMode="numeric" id="pensjonFraUtlandet"/>
                        <ReadMore header="Dette skal du oppgi her">
                            Legg inn pensjoner fra utlandet som et årsbeløp før skatt. Inntekten du oppgir her har bare betydning for størrelsen på barnetillegget ditt.
                        </ReadMore>
                    </VStack>



                <Button type="submit" variant="secondary">
                    send
                </Button>

                <Box padding="4" background="surface-info-subtle"> Din samlede forventede inntekt i {selectedYear}:  {inntektSum} </Box>

                </form>


            </HStack>

            <Heading level="2" size="small">Din inntekt hittil i år ({selectedYear})</Heading>

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