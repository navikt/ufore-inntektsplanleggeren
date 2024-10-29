import { VStack, TextField, ReadMore, Box, ErrorSummary } from "@navikt/ds-react";
import React, { useState } from "react";
import "./FormFields.css";
import { PersonInntekt} from "@/api/apiFetching";

interface FormFieldsProps {
    year?: string;
    errors: Partial<Record<keyof PersonInntekt, string>>;
    setErrors: React.Dispatch<React.SetStateAction<Partial<Record<keyof PersonInntekt, string>>>>;
    setInntekt: (key: keyof PersonInntekt, value: number) => void;
    data: PersonInntekt
    inntektSum: number
}

export const formatInntekt = (amount?: number | string | null): string => {
    if (amount === null || amount === undefined || amount === '') return ''
    const integerAmount =
        typeof amount === 'string'
            ? parseInt(amount.replace(/\D+/g, ''), 10)
            : amount

    return !isNaN(integerAmount)
        ? Intl.NumberFormat('nb-NO', {
            style: 'decimal',
            minimumFractionDigits: 0,
            maximumFractionDigits: 0,
        }).format(integerAmount)
        : ''
}

export const formatInntektToNumber = (s?: string) => {
    if(!s) return 0
    const returnData = Number(s.replace(/\s+/g, ''))
    return returnData
}



export const FormFields = ({ year, errors, setErrors, setInntekt, data, inntektSum }: FormFieldsProps) => {
    React.useEffect(() => {
        window.scrollTo(0, 0);
    }, []);
    const [fieldErrors, setFieldErrors] = useState<Partial<Record<keyof PersonInntekt, string>>>({});

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { id, value } = e.target;
        const numericValue = formatInntektToNumber(value);

        if (isNaN(numericValue) || numericValue < 0 ) { //todo care about Infinity and other weird numbers?
            setFieldErrors((prev) => ({ ...prev, [id]: 'Må være et tall' }));
        } else {
            setFieldErrors((prev) => ({ ...prev, [id]: undefined }));
            setInntekt(id as keyof PersonInntekt, numericValue);
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
                <TextField label="Arbeidsinntekt fra arbeidsgiver" inputMode="numeric" id="arbeidsinntekt" error={fieldErrors.arbeidsinntekt} value={formatInntekt(data.arbeidsinntekt)} onBlur={handleInputChange} onChange={handleInputChange} pattern="[\d\s]+"/>
                <div className="description-card">
                    <ReadMore header="Denne arbeidsinnteken skal med ">
                        Legg inn lønn fra arbeidsgiver som et årsbeløp før skatt. Ta med eventuell bonus og overtidsbetaling og feriepenger som blir utbetalt i {year}.
                    </ReadMore>
                </div>
            </VStack>

            <VStack className="vstack-gap">
                <TextField label="Pensjonsgivende ytelser fra oss/NAV" inputMode="numeric" id="navYtelse" error={fieldErrors.navYtelse}  value={data.navYtelse} onChange={handleInputChange} onBlur={handleInputChange} description="Uføretrygden skal ikke tas med"/>
                <ReadMore header="Disse pensjonsgivende ytelsene skal med">
                    Har du sykepenger, arbeidsavklaringspenger, dagpenger, foreldrepenger, svangerskapspenger, omstillingsstønad, overgangsstønad, ventelønn, omsorgs-, pleie- eller opplæringspenger
                    fra oss, skal du oppgi dette her. Uføretrygden skal ikke tas med.
                </ReadMore>
            </VStack>

            <VStack className="vstack-gap">
                <TextField label="Næringsinntekt" inputMode="numeric" id="naeringsinntekt" error={fieldErrors.naeringsinntekt} value={data.naeringsinntekt} onChange={handleInputChange} onBlur={handleInputChange}/>
                <ReadMore header="Tekst tekst tekst">
                    Legg inn det du forventer å tjene fra næringsvirksomhet i Norge som et årsbeløp før skatt.
                </ReadMore>
            </VStack>

            <VStack className="vstack-gap">
                <TextField label="Inntekt fra utlandet, i norske kroner" inputMode="numeric" id="inntektFraUtlandet" error={fieldErrors.inntektFraUtlandet} value={data.inntektFraUtlandet} onChange={handleInputChange} onBlur={handleInputChange}/>
                <ReadMore header="Tekst tekst tekst">
                    Legg inn det du forventer å tjene i arbeidsinntekt og næringsinntekt fra utlandet som et årsbeløp før skatt.
                </ReadMore>
            </VStack>

            <VStack className="vstack-gap">
                <TextField label="Pensjoner og uførepensjon fra andre enn folketrygden" inputMode="numeric" id="pensjonFraAndre" error={fieldErrors.pensjonFraAndre} value={data.pensjonFraAndre} onChange={handleInputChange} onBlur={handleInputChange}/>
                <ReadMore header="Dette skal du oppgi her">
                    Legg inn pensjoner fra andre enn oss som et årsbeløp før skatt. Oppgi pensjoner fra både offentlige og private ordninger. Dette inkluderer også uførepensjon fra andre enn oss. Har du krigspensjon eller familiepleieytelse
                    fra oss, skal du oppgi dette også her. Ikke oppgi eventuell alderspensjon du mottar fra oss. Inntekten du oppgir her har bare betydning for størrelsen på barnetillegget ditt.
                </ReadMore>
            </VStack>

            <VStack className="vstack-gap">
                <TextField label="Pensjoner fra utlandet, i norske kroner" inputMode="numeric" id="pensjonFraUtlandet" error={fieldErrors.pensjonFraUtlandet} value={data.pensjonFraUtlandet} onChange={handleInputChange}/>
                <ReadMore header="Dette skal du oppgi her">
                    Legg inn pensjoner fra utlandet som et årsbeløp før skatt. Inntekten du oppgir her har bare betydning for størrelsen på barnetillegget ditt.
                </ReadMore>
            </VStack>
            <Box padding="4" background="surface-info-subtle"> Din samlede forventede inntekt i {year}:  {inntektSum} </Box>
        </div>
    );
};