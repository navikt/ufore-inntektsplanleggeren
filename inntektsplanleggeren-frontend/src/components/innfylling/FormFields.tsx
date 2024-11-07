import {VStack, TextField, ReadMore, Box, ErrorSummary, Heading} from "@navikt/ds-react";
import React, {useEffect, useState} from "react";
import "./FormFields.css";
import {PersonInntekter} from "@/api/model/ApiRequests";
import {FormatKroner} from "@/components/utils/FormatKroner";

interface FormFieldsProps {
    year?: string;
    errors: Partial<Record<keyof PersonInntekter, string>>;
    setErrors: React.Dispatch<React.SetStateAction<Partial<Record<keyof PersonInntekter, string>>>>;
    setInntekt: (key: keyof PersonInntekter, value: number) => void;
    inntektSum: number;
    forventedeInntekter: PersonInntekter
}

const FORMATTER = Intl.NumberFormat('nb-NO', {
    style: 'decimal',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0,
});

export const formatInntekt = (amount?: number | string | null): string => {
    if (amount === null || amount === undefined || amount === '') return ''
    const integerAmount =
        typeof amount === 'string'
            ? parseInt(amount.replace(/\D+/g, ''), 10)
            : amount

    return !isNaN(integerAmount) ? FORMATTER.format(integerAmount) : ''
}

export const parseInntekt = (s: string) => {
    if(!s) return 0
    if(s.includes('.')) {
        return NaN
    }
    return Number(s.replace(/\s+/g, ''))
}

export const FormFields = ({ year, errors, setInntekt,  inntektSum, forventedeInntekter }: FormFieldsProps) => {
    const [fieldErrors, setFieldErrors] = useState<Partial<Record<keyof PersonInntekter, string>>>({});
    const [inputData, setInputData] = useState<Partial<Record<keyof PersonInntekter, string>>>({});





    const handleInputChange = (field: keyof PersonInntekter) => ({ target }: React.ChangeEvent<HTMLInputElement>) => {
        // Sjekker tom input
        if (target.value === '') {
            setInputData((prev) => ({ ...prev, [field]: target.value }));
            setFieldErrors((prev) => ({ ...prev, [field]: undefined }));
            setInntekt(field, 0);
            return;
        }
        const numericValue = parseInntekt(target.value);

        if (isNaN(numericValue) || numericValue < 0) {
            setInputData((prev) => ({ ...prev, [field]: target.value }));
            setFieldErrors((prev) => ({ ...prev, [field]: 'Må være hele tall' }));
        } else {
            setInputData((prev) => ({ ...prev, [field]: undefined}));
            setFieldErrors((prev) => ({ ...prev, [field]: undefined }));
            setInntekt(field, numericValue);
        }
    };

    return (
        <div>
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
                <TextField label="Arbeidsinntekt og pensjonsgivende ytelser" inputMode="numeric"  error={fieldErrors.arbeidsinntekt}
                           value={inputData.arbeidsinntekt ?? formatInntekt(forventedeInntekter.arbeidsinntekt)} onBlur={handleInputChange("arbeidsinntekt")} onChange={handleInputChange("arbeidsinntekt")} pattern="[\d\s]+" htmlSize={30} />
                <div className="description-card">
                    <ReadMore header="Denne arbeidsinnteken skal med ">
                        Legg inn lønn fra arbeidsgiver som et årsbeløp før skatt. Ta med eventuell bonus og overtidsbetaling og feriepenger som blir utbetalt i {year}.
                    </ReadMore>
                </div>
            </VStack>

            <VStack className="vstack-gap">
                <TextField label="Næringsinntekt" inputMode="numeric"  error={fieldErrors.naeringsinntekt}
                           value={inputData.naeringsinntekt ?? formatInntekt(forventedeInntekter.naeringsinntekt)} onChange={handleInputChange("naeringsinntekt")} onBlur={handleInputChange("naeringsinntekt")} htmlSize={30}
                           />
                <ReadMore header="Tekst tekst tekst">
                    Legg inn det du forventer å tjene fra næringsvirksomhet i Norge som et årsbeløp før skatt.
                </ReadMore>
            </VStack>

            <VStack className="vstack-gap">
                <TextField label="Inntekt fra utlandet, i norske kroner" inputMode="numeric"  error={fieldErrors.inntektUtland}
                           value={inputData.inntektUtland ?? formatInntekt(forventedeInntekter.inntektUtland)} onChange={handleInputChange("inntektUtland")} onBlur={handleInputChange("inntektUtland")} htmlSize={30}
                           />
                <ReadMore header="Tekst tekst tekst">
                    Legg inn det du forventer å tjene i arbeidsinntekt og næringsinntekt fra utlandet som et årsbeløp før skatt.
                </ReadMore>
            </VStack>

            <VStack className="vstack-gap">
                <TextField label="Pensjonerer og uførepensjoner fra andre enn folketrygden" inputMode="numeric"
                           error={fieldErrors.andrePensjonsgivendeYtelser} value={inputData.andrePensjonsgivendeYtelser ?? formatInntekt(forventedeInntekter.andrePensjonsgivendeYtelser)} onChange={handleInputChange("andrePensjonsgivendeYtelser")} onBlur={handleInputChange("andrePensjonsgivendeYtelser")} htmlSize={30}
                           />
                <ReadMore header="Dette skal du oppgi her">
                    Legg inn pensjoner fra andre enn oss som et årsbeløp før skatt. Oppgi pensjoner fra både offentlige og private ordninger. Dette inkluderer også uførepensjon fra andre enn oss. Har du krigspensjon eller familiepleieytelse
                    fra oss, skal du oppgi dette også her. Ikke oppgi eventuell alderspensjon du mottar fra oss. Inntekten du oppgir her har bare betydning for størrelsen på barnetillegget ditt.
                </ReadMore>
            </VStack>

            <VStack className="vstack-gap">
                <TextField label="Pensjoner fra utlandet, i norske kroner" inputMode="numeric"
                           error={fieldErrors.pensjonUtland} value={inputData.pensjonUtland ?? formatInntekt(forventedeInntekter.pensjonUtland)} onChange={handleInputChange("pensjonUtland")} onBlur={handleInputChange("pensjonUtland")} htmlSize={30}
                           />
                <ReadMore header="Dette skal du oppgi her">
                    Legg inn pensjoner fra utlandet som et årsbeløp før skatt. Inntekten du oppgir her har bare betydning for størrelsen på barnetillegget ditt.
                </ReadMore>
            </VStack>

            <Box padding="4" background="surface-info-subtle">
                <VStack>
                    <Heading size="small"> Din samlede forventede inntekt i {year}: </Heading>
                    <p className="sum"><FormatKroner value={inntektSum}/></p>
                </VStack>
            </Box>
        </div>
    );
};