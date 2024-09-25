import { VStack, TextField, ReadMore, Button, Box, ErrorSummary } from "@navikt/ds-react";
import React, { useState } from "react";
import "./FormFields.css";
import { PersonInntekt } from "@/api/apiFetching";

interface FormFieldsProps {
    year: string;
    errors: Partial<Record<keyof PersonInntekt, string>>;
    setErrors: React.Dispatch<React.SetStateAction<Partial<Record<keyof PersonInntekt, string>>>>;
    setPersonInntekt: (personInntekt: PersonInntekt) => void;
}

export const FormFields = ({ year, errors, setErrors, setPersonInntekt }: FormFieldsProps) => {
    const [inntektSum, setInntektSum] = useState<number>(0);
    const [fieldErrors, setFieldErrors] = useState<Partial<Record<keyof PersonInntekt, string>>>({});

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { id, value } = e.target;
        const numericValue = Number(value);

        if (isNaN(numericValue) || numericValue < 0 ) { //todo care about Infinity and other weird numbers?
            setFieldErrors((prev) => ({ ...prev, [id]: 'Må være et tall' }));
        } else {
            setFieldErrors((prev) => ({ ...prev, [id]: undefined }));
        }
    };

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
            if (isNaN(value as number)) {
                return {
                    ...acc,
                    [key]: 'Må være et tall'
                };
            } else if (value < 0) {
                return {
                    ...acc,
                    [key]: 'Må ikke være mindre enn 0'
                };
            }
            return acc;
        }, {} as Partial<Record<keyof PersonInntekt, string>>);

        setFieldErrors(newErrors);

        if (Object.keys(newErrors).length === 0) {
            updateInntektSum(formData);
            setPersonInntekt(formData);
        }
    };

    function updateInntektSum(formData: PersonInntekt) {
        const sum = Object.values(formData).reduce((acc, value) => acc + (value as number), 0);
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

            <VStack className="vstack-gap">
                <TextField label="Arbeidsinntekt fra arbeidsgiver" inputMode="numeric" id="arbeidsinntekt" error={fieldErrors.arbeidsinntekt} onChange={handleInputChange} pattern="[\d\s]+"/>
                {fieldErrors.arbeidsinntekt && <div className="error-message">{fieldErrors.arbeidsinntekt}</div>}
                <div className="description-card">
                    <ReadMore header="Denne arbeidsinnteken skal med ">
                        Legg inn lønn fra arbeidsgiver som et årsbeløp før skatt. Ta med eventuell bonus og overtidsbetaling og feriepenger som blir utbetalt i {year}.
                    </ReadMore>
                </div>
            </VStack>

            <VStack className="vstack-gap">
                <TextField label="Pensjonsgivende ytelser fra oss/NAV" inputMode="numeric" id="navYtelse" error={fieldErrors.navYtelse} onChange={handleInputChange} description="Uføretrygden skal ikke tas med"/>
                {fieldErrors.navYtelse && <div className="error-message">{fieldErrors.navYtelse}</div>}
                <ReadMore header="Disse pensjonsgivende ytelsene skal med">
                    Har du sykepenger, arbeidsavklaringspenger, dagpenger, foreldrepenger, svangerskapspenger, omstillingsstønad, overgangsstønad, ventelønn, omsorgs-, pleie- eller opplæringspenger
                    fra oss, skal du oppgi dette her. Uføretrygden skal ikke tas med.
                </ReadMore>
            </VStack>

            <VStack className="vstack-gap">
                <TextField label="Næringsinntekt" inputMode="numeric" id="naeringsinntekt" error={fieldErrors.naeringsinntekt} onChange={handleInputChange}/>
                {fieldErrors.naeringsinntekt && <div className="error-message">{fieldErrors.naeringsinntekt}</div>}
                <ReadMore header="Tekst tekst tekst">
                    Legg inn det du forventer å tjene fra næringsvirksomhet i Norge som et årsbeløp før skatt.
                </ReadMore>
            </VStack>

            <VStack className="vstack-gap">
                <TextField label="Inntekt fra utlandet, i norske kroner" inputMode="numeric" id="inntektFraUtlandet" error={fieldErrors.inntektFraUtlandet} onChange={handleInputChange}/>
                {fieldErrors.inntektFraUtlandet && <div className="error-message">{fieldErrors.inntektFraUtlandet}</div>}
                <ReadMore header="Tekst tekst tekst">
                    Legg inn det du forventer å tjene i arbeidsinntekt og næringsinntekt fra utlandet som et årsbeløp før skatt.
                </ReadMore>
            </VStack>

            <VStack className="vstack-gap">
                <TextField label="Pensjoner og uførepensjon fra andre enn folketrygden" inputMode="numeric" id="pensjonFraAndre" error={fieldErrors.pensjonFraAndre} onChange={handleInputChange}/>
                {fieldErrors.pensjonFraAndre && <div className="error-message">{fieldErrors.pensjonFraAndre}</div>}
                <ReadMore header="Dette skal du oppgi her">
                    Legg inn pensjoner fra andre enn oss som et årsbeløp før skatt. Oppgi pensjoner fra både offentlige og private ordninger. Dette inkluderer også uførepensjon fra andre enn oss. Har du krigspensjon eller familiepleieytelse
                    fra oss, skal du oppgi dette også her. Ikke oppgi eventuell alderspensjon du mottar fra oss. Inntekten du oppgir her har bare betydning for størrelsen på barnetillegget ditt.
                </ReadMore>
            </VStack>

            <VStack className="vstack-gap">
                <TextField label="Pensjoner fra utlandet, i norske kroner" inputMode="numeric" id="pensjonFraUtlandet" error={fieldErrors.pensjonFraUtlandet} onChange={handleInputChange}/>
                {fieldErrors.pensjonFraUtlandet && <div className="error-message">{fieldErrors.pensjonFraUtlandet}</div>}
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