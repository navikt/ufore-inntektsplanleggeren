import {VStack, TextField, ReadMore, Box, Heading, BodyShort, BodyLong} from "@navikt/ds-react";
import React, { useState } from "react";
import "./FormFields.css";
import { PersonInntekter } from "@/api/model/ApiRequests";
import { FormatKroner } from "@/components/utils/FormatKroner";
import {formatInntekt, parseInntekt} from "@/components/utils/FormatNumbersUtil";

export interface FormFieldsProps {
    year?: number;
    errors: Partial<Record<keyof PersonInntekter, string>>;
    setErrors: React.Dispatch<React.SetStateAction<Partial<Record<keyof PersonInntekter, string>>>>;
    setInntekt: (key: keyof PersonInntekter, value: number) => void;
    inntektSum: number;
    forventedeInntekter: PersonInntekter;
}

export const FormFieldsEps = ({ year, errors, setInntekt, inntektSum, forventedeInntekter }: FormFieldsProps) => {
    const [fieldErrors, setFieldErrors] = useState<Partial<Record<keyof PersonInntekter, string>>>(errors);
    const [inputData, setInputData] = useState<Partial<Record<keyof PersonInntekter, string>>>({});

    const handleInputChange = (field: keyof PersonInntekter) => ({ target }: React.ChangeEvent<HTMLInputElement>) => {
        if (target.value === '') {
            setInputData((prev) => ({ ...prev, [field]: target.value }));
            setFieldErrors((prev) => ({ ...prev, [field]: undefined }));
            setInntekt(field, 0);
            return;
        }
        const numericValue = parseInntekt(target.value);

        if (isNaN(numericValue) || numericValue < 0) {
            setInputData((prev) => ({ ...prev, [field]: target.value }));
            setFieldErrors((prev) => ({ ...prev, [field]: 'Du kan ikke skrive mellomrom, bokstaver eller tegn' }));
        } else {
            setInputData((prev) => ({ ...prev, [field]: undefined }));
            setFieldErrors((prev) => ({ ...prev, [field]: undefined }));
            setInntekt(field, numericValue);
        }
    };

    return (
        <div>
            {forventedeInntekter.arbeidsinntekt !== null && (
                <VStack className="vstack-gap">
                    <TextField
                        label="Lønn og pensjonsgivende ytelser"
                        description="Du skal ikke ta med uføretrygden."
                        inputMode="numeric"
                        error={fieldErrors.arbeidsinntekt ?? errors.arbeidsinntekt}
                        value={inputData.arbeidsinntekt ?? formatInntekt(forventedeInntekter.arbeidsinntekt)}
                        onBlur={handleInputChange("arbeidsinntekt")}
                        onChange={handleInputChange("arbeidsinntekt")}
                        pattern="[\d\s]+"
                        htmlSize={30}
                    />
                    <div className="description-card">
                        <ReadMore header="Innteker du skal legge inn">
                            Legg inn annen forelders lønn fra arbeidsgiver som et årsbeløp. Ta med eventuell bonus, ekstratimer, overtidsbetaling og feriepenger for det gjeldende året.
                            Vanlige pensjonsgivende ytelser er sykepenger, arbeidsavklaringspenger (AAP), dagpenger, foreldrepenger, svangerskapspenger,  overgangsstønad, omsorgs-, pleie-
                            eller opplæringspenger fra oss. Er du usikker på om en inntekt eller pengestøtte er pensjonsgivende kan du kontakte Skatteetaten.
                        </ReadMore>
                    </div>
                </VStack>
            )}

            {forventedeInntekter.naeringsinntekt !== null && (
                <VStack className="vstack-gap">
                    <TextField
                        label="Næringsinntekt"
                        inputMode="numeric"
                        error={fieldErrors.naeringsinntekt}
                        value={inputData.naeringsinntekt ?? formatInntekt(forventedeInntekter.naeringsinntekt)}
                        onChange={handleInputChange("naeringsinntekt")}
                        onBlur={handleInputChange("naeringsinntekt")}
                        htmlSize={30}
                    />
                    <ReadMore header="Næringsinntekt du skal legge inn">
                        Legg inn det den andre forelderen forventer å tjene fra næringsvirksomhet i Norge som et årsbeløp før skatt. Er du usikker på hva som regnes som pensjonsgivende næringsinntekt
                        kan du kontakte Skatteetaten.
                    </ReadMore>
                </VStack>
            )}

            {forventedeInntekter.inntektUtland !== null && (
                <VStack className="vstack-gap">
                    <TextField
                        label="Inntekt fra utlandet"
                        description="I norske kroner"
                        inputMode="numeric"
                        error={fieldErrors.inntektUtland}
                        value={inputData.inntektUtland ?? formatInntekt(forventedeInntekter.inntektUtland)}
                        onChange={handleInputChange("inntektUtland")}
                        onBlur={handleInputChange("inntektUtland")}
                        htmlSize={30}
                    />
                    <ReadMore header="Inntekter du skal melde inn">
                        Legg inn det den andre forelderen forventer å tjene i arbeidsinntekt og næringsinntekt fra utlandet som et årsbeløp før skatt.
                    </ReadMore>
                </VStack>
            )}

            {forventedeInntekter.andrePensjonsgivendeYtelser !== null && (
                <VStack className="vstack-gap">
                    <TextField
                        label="Pensjoner og uførepensjon fra andre enn Nav"
                        description="For eksempel fra KLP, OPF, SPK, Gjensidige, Storebrand"
                        inputMode="numeric"
                        error={fieldErrors.andrePensjonsgivendeYtelser ?? errors.andrePensjonsgivendeYtelser}
                        value={inputData.andrePensjonsgivendeYtelser ?? formatInntekt(forventedeInntekter.andrePensjonsgivendeYtelser)}
                        onChange={handleInputChange("andrePensjonsgivendeYtelser")}
                        onBlur={handleInputChange("andrePensjonsgivendeYtelser")}
                        htmlSize={30}
                    />
                    <ReadMore header="Pensjoner du skal legge inn">
                        <VStack gap="4">
                            <BodyLong> Legg inn annen forelders pensjoner som et årsbeløp før skatt. Oppgi pensjoner fra både private og offentlige ordninger.</BodyLong>
                            <BodyLong>Du skal ikke melde inn</BodyLong>
                            <ul>
                                <li>alderspensjon fra oss</li>
                                <li>uføretrygd fra oss</li>
                                <li>AFP i privat sektor</li>
                                <li>AFP fra Statens pensjonskasse hvis du er under 65 år</li>
                            </ul>

                            <BodyLong>Du skal for eksempel melde inn</BodyLong>
                            <ul>
                                <li>AFP offentlig</li>
                                <li>uførepensjon</li>
                                <li>introduksjonsstønad</li>
                                <li>barnepensjon</li>
                                <li>supplerende stønad</li>
                            </ul>
                        </VStack>
                    </ReadMore>
                </VStack>
            )}

            {forventedeInntekter.pensjonUtland !== null && (
                <VStack className="vstack-gap">
                    <TextField
                        label="Pensjoner fra utlandet"
                        description="I norske kroner"
                        inputMode="numeric"
                        error={fieldErrors.pensjonUtland}
                        value={inputData.pensjonUtland ?? formatInntekt(forventedeInntekter.pensjonUtland)}
                        onChange={handleInputChange("pensjonUtland")}
                        onBlur={handleInputChange("pensjonUtland")}
                        htmlSize={30}
                    />
                    <ReadMore header="Pensjoner du skal melde inn">
                        Legg inn den andre forelderens pensjoner fra utlandet som et årsbeløp.
                    </ReadMore>
                </VStack>
            )}

            <Box padding="4" background="surface-info-subtle">
                <VStack>
                    <Heading size="small"> Annen forelder sin samlede inntekt i {year}: </Heading>
                    <BodyShort className="sum"><FormatKroner value={inntektSum}/> før skatt</BodyShort>
                </VStack>
            </Box>
        </div>
    );
};