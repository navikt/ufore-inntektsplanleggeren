import { VStack, TextField, ReadMore, Button, Box, ErrorSummary } from "@navikt/ds-react";
import React, { useState } from "react";
import {PersonInntekt} from "@/api/apiFetching";



interface FormFieldsProps {
    year: number;
    errors: Partial<Record<keyof PersonInntekt, string>>;
    setErrors: React.Dispatch<React.SetStateAction<Partial<Record<keyof PersonInntekt, string>>>>;
    setPersonInntekt: (personInntekt: PersonInntekt) => void;
}

export const FormFields = ({ year, errors, setErrors,  setPersonInntekt }: FormFieldsProps) => {
    const [inntektSum, setInntektSum] = useState<number>(0);

    const handleSubmit: React.FormEventHandler<HTMLFormElement> = (e) => {
        e.preventDefault();
        const formData: PersonInntekt = {
            arbeidsinntekt: parseFloat(e.currentTarget.arbeidsinntekt.value),
            navYtelse: parseFloat(e.currentTarget.navYtelse.value),
            naeringsinntekt: parseFloat(e.currentTarget.naeringsinntekt.value),
            inntektFraUtlandet: parseFloat(e.currentTarget.inntektFraUtlandet.value),
            pensjonFraAndre: parseFloat(e.currentTarget.pensjonFraAndre.value),
            pensjonFraUtlandet: parseFloat(e.currentTarget.pensjonFraUtlandet.value)
        };

        const newErrors = Object.entries(formData).reduce((acc, [key, value]) => {
            if (value === '') {
                return {
                    ...acc,
                    [key]: 'Feltet er påkrevd'
                }
            } else if (isNaN(value)) {
                return {
                    ...acc,
                    [key]: 'Maa vaere tall'
                }
            } else if (value < 0) {
                return {
                    ...acc,
                    [key]: 'Må ikke være mindre enn 0'
                }
            }
            return acc;
        }, {});

        setErrors(newErrors);

        if (Object.keys(newErrors).length === 0) {
            updateInntektSum(formData);
        }

        setPersonInntekt(formData);
    };

    function updateInntektSum(formData: PersonInntekt) {
        const sum = Object.values(formData).reduce((acc, value) => acc + value, 0);
        setInntektSum(sum);
    }

    return (
        <form onSubmit={handleSubmit}>
            {Object.keys(errors).length > 0 && (
                <ErrorSummary heading="Du må rette disse feilene før du kan sende inn søknaden:">
                    {Object.entries(errors).map(([key, value]) => (
                        <ErrorSummary.Item href={`#${key}`} key={key}>
                            {value}
                        </ErrorSummary.Item>
                    ))}
                </ErrorSummary>
            )}

            <VStack gap={"5 5"}>
                <TextField label="Arbeidsinntekt fra arbeidsgiver" inputMode="numeric" id="arbeidsinntekt"/>
                <div className="description-card">
                    <ReadMore header="Denne arbeidsinnteken skal med ">
                        Legg inn lønn fra arbeidsgiver som et årsbeløp før skatt. Ta med eventuell bonus og overtidsbetaling og feriepenger som blir utbetalt i {year}.
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
            <Box padding="4" background="surface-info-subtle"> Din samlede forventede inntekt i {year}:  {inntektSum} </Box>
        </form>
    );
};