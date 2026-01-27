import { BodyLong, BodyShort, Box, Heading, List, ReadMore, TextField, VStack } from '@navikt/ds-react'
import React, { useState } from 'react'
import './FormFields.css'
import { PersonInntekter } from '@/api/model/ApiRequests'
import { FormatKroner } from '@/components/utils/FormatKroner'
import { formatInntekt, parseInntekt } from '@/components/utils/FormatNumbersUtil'

export interface FormFieldsProps {
    year?: number
    errors: Partial<Record<keyof PersonInntekter, string>>
    setErrors: React.Dispatch<React.SetStateAction<Partial<Record<keyof PersonInntekter, string>>>>
    setInntekt: (key: keyof PersonInntekter, value: number) => void
    inntektSum: number
    forventedeInntekter: PersonInntekter
}

export const FormFieldsEps = ({ year, errors, setErrors, setInntekt, inntektSum, forventedeInntekter }: FormFieldsProps) => {
    const [fieldErrors, setFieldErrors] = useState<Partial<Record<keyof PersonInntekter, string>>>(errors)
    const [inputData, setInputData] = useState<Partial<Record<keyof PersonInntekter, string>>>({})
    const MAX_VALUE = 2147483647

    const handleInputChange =
        (field: keyof PersonInntekter) =>
        ({ target }: React.ChangeEvent<HTMLInputElement>) => {
            if (target.value === '') {
                setInputData((prev) => ({ ...prev, [field]: target.value }))
                setErrorOnState(field, undefined)
                setInntekt(field, 0)
                return
            }
            const numericValue = parseInntekt(target.value)

            if (isNaN(numericValue) || numericValue < 0) {
                setInputData((prev) => ({ ...prev, [field]: target.value }))
                setErrorOnState(field, 'Du kan ikke skrive mellomrom, bokstaver eller tegn')
            } else if (numericValue > MAX_VALUE) {
                setInputData((prev) => ({ ...prev, [field]: target.value }))
                setErrorOnState(field, 'Tallet du har skrevet inn er for stort')
            } else {
                setInputData((prev) => ({ ...prev, [field]: undefined }))
                setErrorOnState(field, undefined)
                setInntekt(field, numericValue)
            }
        }

    const setErrorOnState = (field: keyof PersonInntekter, message: string | undefined) => {
        setFieldErrors((prev) => ({ ...prev, [field]: message }))
        setErrors((prev) => ({ ...prev, [field]: message }))
    }

    return (
        <div className="form-fields-wrapper">
            {forventedeInntekter.arbeidsinntekt !== null && (
                <VStack className="vstack-gap">
                    <TextField
                        id="arbeidsinntekt_eps"
                        label="Lønn, fordeler og noen pengestøtter fra Nav"
                        description="Uføretrygd skal ikke tas med. Se hva du skal legge inn i beskrivelsen under. Den andre forelderen kan se inntekt som er registrert hittil i år hos Skatteetaten."
                        inputMode="numeric"
                        error={fieldErrors.arbeidsinntekt ?? errors.arbeidsinntekt}
                        value={inputData.arbeidsinntekt ?? formatInntekt(forventedeInntekter.arbeidsinntekt)}
                        onBlur={handleInputChange('arbeidsinntekt')}
                        onChange={handleInputChange('arbeidsinntekt')}
                        pattern="[\d\s]+"
                        htmlSize={30}
                    />
                    <div>
                        <ReadMore header="Inntekt du skal legge inn">
                            <p>Du skal legge inn annen forelders pensjonsgivende inntekter.</p>
                            <BodyShort>Lønn og fordeler du skal legge inn</BodyShort>
                            <div className="listCompact">
                                <List>
                                    <List.Item>lønn fra arbeidsgiver</List.Item>
                                    <List.Item>bonus, ekstratimer, overtidsbetaling og feriepenger</List.Item>
                                    <List.Item>skattepliktige fordeler fra arbeidsgiver (for eksempel kost og losji, lån fra arbeidsgiver, telefon)</List.Item>
                                    <List.Item>honorar og godtgjørelser</List.Item>
                                    <List.Item>andre pensjonsgivende inntekter</List.Item>
                                </List>
                            </div>
                            <BodyShort>Pengestøtter fra Nav du skal legge inn</BodyShort>
                            <div className="listCompact">
                                <List>
                                    <List.Item>sykepenger</List.Item>
                                    <List.Item>arbeidsavklaringspenger (AAP)</List.Item>
                                    <List.Item>dagpenger</List.Item>
                                    <List.Item>foreldrepenger</List.Item>
                                    <List.Item>overgangsstønad</List.Item>
                                    <List.Item>svangerskapspenger</List.Item>
                                    <List.Item>omsorgs-, pleie- eller opplæringspenger</List.Item>
                                    <List.Item>andre pengestøtter fra Nav som er pensjonsgivende</List.Item>
                                </List>
                            </div>
                            <BodyShort>Du skal ikke legge inn</BodyShort>
                            <div className="listCompact">
                                <List>
                                    <List.Item>uføretrygd</List.Item>
                                    <List.Item>barnetrygd</List.Item>
                                    <List.Item>alderspensjon</List.Item>
                                    <List.Item>uførepensjon fra andre enn Nav skal oppgis i et annet felt</List.Item>
                                    <List.Item>kapitalinntekt</List.Item>
                                </List>
                            </div>
                            Er du usikker på om en inntekt eller pengestøtte er pensjonsgivende kan du kontakte Skatteetaten.
                        </ReadMore>
                    </div>
                </VStack>
            )}
            {forventedeInntekter.naeringsinntekt !== null && (
                <VStack className="vstack-gap">
                    <TextField
                        id="naeringsinntekt_eps"
                        label="Næringsinntekt"
                        inputMode="numeric"
                        error={fieldErrors.naeringsinntekt}
                        value={inputData.naeringsinntekt ?? formatInntekt(forventedeInntekter.naeringsinntekt)}
                        onChange={handleInputChange('naeringsinntekt')}
                        onBlur={handleInputChange('naeringsinntekt')}
                        htmlSize={30}
                    />
                    <ReadMore header="Næringsinntekt du skal legge inn">
                        Legg inn det den andre forelderen forventer å tjene fra næringsvirksomhet i Norge før skatt. Er du usikker på hva som regnes som
                        pensjonsgivende næringsinntekt kan du kontakte Skatteetaten.
                    </ReadMore>
                </VStack>
            )}
            {forventedeInntekter.inntektUtland !== null && (
                <VStack className="vstack-gap">
                    <TextField
                        id="inntektUtland_eps"
                        label="Inntekt fra utlandet"
                        description="I norske kroner"
                        inputMode="numeric"
                        error={fieldErrors.inntektUtland}
                        value={inputData.inntektUtland ?? formatInntekt(forventedeInntekter.inntektUtland)}
                        onChange={handleInputChange('inntektUtland')}
                        onBlur={handleInputChange('inntektUtland')}
                        htmlSize={30}
                    />
                    <ReadMore header="Inntekt du skal legge inn">
                        Legg inn det den andre forelderen forventer å tjene i arbeidsinntekt og næringsinntekt fra utlandet før skatt.
                    </ReadMore>
                </VStack>
            )}
            {forventedeInntekter.andrePensjonsgivendeYtelser !== null && (
                <VStack className="vstack-gap">
                    <TextField
                        id="andrePensjonsgivendeYtelser_eps"
                        label="Uførepensjon og pensjoner fra andre enn Nav"
                        description="For eksempel fra KLP, OPF, SPK, Gjensidige, Storebrand. Den andre forelderen kan se pensjon som er registrert hittil i år hos Skatteetaten."
                        inputMode="numeric"
                        error={fieldErrors.andrePensjonsgivendeYtelser ?? errors.andrePensjonsgivendeYtelser}
                        value={inputData.andrePensjonsgivendeYtelser ?? formatInntekt(forventedeInntekter.andrePensjonsgivendeYtelser)}
                        onChange={handleInputChange('andrePensjonsgivendeYtelser')}
                        onBlur={handleInputChange('andrePensjonsgivendeYtelser')}
                        htmlSize={30}
                    />
                    <ReadMore header="Pensjoner du skal legge inn" className="readmoreCompact">
                        <VStack gap="space-16">
                            <BodyLong>Legg inn annen forelders pensjoner før skatt. Legg inn pensjoner fra både private og offentlige ordninger.</BodyLong>
                            <div>
                                <BodyShort>Du skal ikke legge inn</BodyShort>
                                <div className="listCompact">
                                    <List>
                                        <List.Item>alderspensjon fra oss</List.Item>
                                        <List.Item>uføretrygd fra oss</List.Item>
                                        <List.Item>AFP i privat sektor</List.Item>
                                        <List.Item>AFP fra Statens pensjonskasse hvis du er under 65 år</List.Item>
                                    </List>
                                </div>
                            </div>
                            <div>
                                <BodyShort>Du skal for eksempel legge inn</BodyShort>
                                <div className="listCompact">
                                    <List>
                                        <List.Item>AFP offentlig</List.Item>
                                        <List.Item>uførepensjon fra andre enn oss</List.Item>
                                        <List.Item>introduksjonsstønad</List.Item>
                                        <List.Item>barnepensjon</List.Item>
                                        <List.Item>supplerende stønad</List.Item>
                                    </List>
                                </div>
                            </div>
                        </VStack>
                    </ReadMore>
                </VStack>
            )}
            {forventedeInntekter.pensjonUtland !== null && (
                <VStack className="vstack-gap">
                    <TextField
                        id="pensjonUtland_eps"
                        label="Pensjoner fra utlandet"
                        description="I norske kroner"
                        inputMode="numeric"
                        error={fieldErrors.pensjonUtland}
                        value={inputData.pensjonUtland ?? formatInntekt(forventedeInntekter.pensjonUtland)}
                        onChange={handleInputChange('pensjonUtland')}
                        onBlur={handleInputChange('pensjonUtland')}
                        htmlSize={30}
                    />
                    <ReadMore header="Pensjoner du skal legge inn">Legg inn den andre forelderens pensjoner fra utlandet før skatt.</ReadMore>
                </VStack>
            )}
            <Box padding="space-16" background="neutral-soft" borderRadius="8">
                <VStack gap={{ xs: 'space-8', sm: 'space-4' }}>
                    <Heading level="4" size="small">
                        Annen forelder sin samlede inntekt i {year}:
                    </Heading>
                    <BodyShort className="sum">
                        <FormatKroner value={inntektSum} /> før skatt
                    </BodyShort>
                </VStack>
            </Box>
        </div>
    )
}
