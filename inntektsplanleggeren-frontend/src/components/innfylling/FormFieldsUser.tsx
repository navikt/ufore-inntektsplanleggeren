import { VStack, TextField, ReadMore, Box, Heading, BodyShort, BodyLong, List } from '@navikt/ds-react'
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

export const FormFieldsUser = ({ year, errors, setErrors, setInntekt, inntektSum, forventedeInntekter }: FormFieldsProps) => {
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
                        id="arbeidsinntekt_bruker"
                        label="Lønn, fordeler og noen pengestøtter fra Nav"
                        description="Du skal ikke legge inn uføretrygden. Se hva du skal legge inn i beskrivelsen under."
                        inputMode="numeric"
                        error={fieldErrors.arbeidsinntekt ?? errors.arbeidsinntekt}
                        value={inputData.arbeidsinntekt ?? formatInntekt(forventedeInntekter.arbeidsinntekt)}
                        onBlur={handleInputChange('arbeidsinntekt')}
                        onChange={handleInputChange('arbeidsinntekt')}
                        pattern="[\d\s]+"
                        htmlSize={30}
                    />
                    <div className="description-card">
                        <ReadMore header="Inntekt du skal legge inn">
                            <p>Du skal legge inn pensjonsgivende inntekter.</p>
                            <BodyShort>Lønn og fordeler du skal legge inn</BodyShort>
                            <List className="listCompact">
                                <List.Item>lønn fra arbeidsgiver</List.Item>
                                <List.Item>bonus, ekstratimer, overtidsbetaling og feriepenger</List.Item>
                                <List.Item>skattepliktige fordeler fra arbeidsgiver (for eksempel kost og losji, lån fra arbeidsgiver, telefon)</List.Item>
                                <List.Item>honorar og godtgjørelser</List.Item>
                                <List.Item>andre pensjonsgivende inntekter</List.Item>
                            </List>
                            Pengestøtter fra Nav du skal legge inn
                            <List className="listCompact">
                                <List.Item>sykepenger</List.Item>
                                <List.Item>arbeidsavklaringspenger (AAP)</List.Item>
                                <List.Item>dagpenger</List.Item>
                                <List.Item>omstillingsstønad</List.Item>
                                <List.Item>foreldrepenger</List.Item>
                                <List.Item>overgangsstønad</List.Item>
                                <List.Item>svangerskapspenger</List.Item>
                                <List.Item>omsorgs-, pleie- eller opplæringspenger</List.Item>
                                <List.Item>andre pengestøtter fra Nav som er pensjonsgivende</List.Item>
                            </List>
                            Du skal ikke legge inn
                            <List className="listCompact">
                                <List.Item>uføretrygd</List.Item>
                                <List.Item>alderspensjon</List.Item>
                                <List.Item>uførepensjon fra andre enn Nav skal oppgis i et annet felt dersom du har barnetillegg</List.Item>
                                <List.Item>kapitalinntekt</List.Item>
                            </List>
                            Er du usikker på om en inntekt eller pengestøtte er pensjonsgivende kan du kontakte Skatteetaten.
                        </ReadMore>
                    </div>
                </VStack>
            )}

            {forventedeInntekter.naeringsinntekt !== null && (
                <VStack className="vstack-gap">
                    <TextField
                        id="naeringsinntekt_bruker"
                        label="Næringsinntekt"
                        inputMode="numeric"
                        error={fieldErrors.naeringsinntekt}
                        value={inputData.naeringsinntekt ?? formatInntekt(forventedeInntekter.naeringsinntekt)}
                        onChange={handleInputChange('naeringsinntekt')}
                        onBlur={handleInputChange('naeringsinntekt')}
                        htmlSize={30}
                    />
                    <ReadMore header="Næringsinntekt du skal legge inn">
                        Legg inn det du forventer å tjene fra næringsvirksomhet i Norge før skatt. Er du usikker på hva som regnes som pensjonsgivende
                        næringsinntekt kan du kontakte Skatteetaten.
                    </ReadMore>
                </VStack>
            )}

            {forventedeInntekter.inntektUtland !== null && (
                <VStack className="vstack-gap">
                    <TextField
                        id="inntektUtland_bruker"
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
                        Legg inn det du forventer å tjene i arbeidsinntekt og næringsinntekt fra utlandet før skatt.
                    </ReadMore>
                </VStack>
            )}

            {forventedeInntekter.andrePensjonsgivendeYtelser !== null && (
                <VStack className="vstack-gap">
                    <TextField
                        id="andrePensjonsgivendeYtelser_bruker"
                        label="Uførepensjon og pensjoner fra andre enn Nav"
                        description="For eksempel fra KLP, OPF, SPK, Gjensidige, Storebrand"
                        inputMode="numeric"
                        error={fieldErrors.andrePensjonsgivendeYtelser ?? errors.andrePensjonsgivendeYtelser}
                        value={inputData.andrePensjonsgivendeYtelser ?? formatInntekt(forventedeInntekter.andrePensjonsgivendeYtelser)}
                        onChange={handleInputChange('andrePensjonsgivendeYtelser')}
                        onBlur={handleInputChange('andrePensjonsgivendeYtelser')}
                        htmlSize={30}
                    />
                    <ReadMore header="Pensjoner du skal legge inn" className="listCompact">
                        <VStack gap="4">
                            <BodyLong>Legg inn pensjoner og uførepensjon fra andre enn oss, før skatt.</BodyLong>
                            <List as="ul" description="Du skal ikke legge inn">
                                <List.Item>alderspensjon fra oss</List.Item>
                                <List.Item>uføretrygd fra oss</List.Item>
                            </List>
                            <List description="Du skal for eksempel legge inn">
                                <List.Item>uførepensjon fra andre enn oss</List.Item>
                                <List.Item>tjenestepensjon</List.Item>
                            </List>
                            <BodyLong>Inntekten du oppgir her har bare betydning for størrelsen på barnetillegget ditt.</BodyLong>
                        </VStack>
                    </ReadMore>
                </VStack>
            )}

            {forventedeInntekter.pensjonUtland !== null && (
                <VStack className="vstack-gap">
                    <TextField
                        id="pensjonUtland_bruker"
                        label="Pensjoner fra utlandet"
                        description="I norske kroner"
                        inputMode="numeric"
                        error={fieldErrors.pensjonUtland}
                        value={inputData.pensjonUtland ?? formatInntekt(forventedeInntekter.pensjonUtland)}
                        onChange={handleInputChange('pensjonUtland')}
                        onBlur={handleInputChange('pensjonUtland')}
                        htmlSize={30}
                    />
                    <ReadMore header="Pensjoner du skal legge inn">
                        Legg inn pensjoner fra utlandet før skatt. Inntekten du oppgir her har bare betydning for størrelsen på barnetillegget ditt.
                    </ReadMore>
                </VStack>
            )}

            <Box padding="4" background="surface-subtle" borderRadius="large">
                <VStack gap={{ xs: '2', sm: '1' }}>
                    <Heading level="4" size="small">
                        {' '}
                        Din samlede forventede inntekt i {year}:{' '}
                    </Heading>
                    <BodyShort className="sum">
                        <FormatKroner value={inntektSum} /> før skatt
                    </BodyShort>
                </VStack>
            </Box>
        </div>
    )
}
