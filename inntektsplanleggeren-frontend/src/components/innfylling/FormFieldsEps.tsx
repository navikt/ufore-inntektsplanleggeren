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

export const FormFieldsEps = ({
  year,
  errors,
  setErrors,
  setInntekt,
  inntektSum,
  forventedeInntekter,
}: FormFieldsProps) => {
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
            label="Lønn og pensjonsgivende ytelser"
            description="Uføretrygd skal ikke tas med. Den andre forelderen kan se inntekt som er registrert hittil i år hos Skatteetaten."
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
              Legg inn annen forelders lønn fra arbeidsgiver som et årsbeløp. Ta med eventuell bonus, ekstratimer,
              overtidsbetaling og feriepenger for det gjeldende året. Inntekter som er pensjonsgivende skal meldes inn.
              Vanlige pensjonsgivende ytelser er sykepenger, arbeidsavklaringspenger (AAP), dagpenger, foreldrepenger,
              svangerskapspenger, overgangsstønad, omsorgs-, pleie- eller opplæringspenger fra oss. Er du usikker på om
              en inntekt eller pengestøtte er pensjonsgivende kan du kontakte Skatteetaten.
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
            Legg inn det den andre forelderen forventer å tjene fra næringsvirksomhet i Norge som et årsbeløp før skatt.
            Er du usikker på hva som regnes som pensjonsgivende næringsinntekt kan du kontakte Skatteetaten.
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
            Legg inn det den andre forelderen forventer å tjene i arbeidsinntekt og næringsinntekt fra utlandet som et
            årsbeløp før skatt.
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
            value={
              inputData.andrePensjonsgivendeYtelser ?? formatInntekt(forventedeInntekter.andrePensjonsgivendeYtelser)
            }
            onChange={handleInputChange('andrePensjonsgivendeYtelser')}
            onBlur={handleInputChange('andrePensjonsgivendeYtelser')}
            htmlSize={30}
          />
          <ReadMore header="Pensjoner du skal legge inn" className="readmorePensjonFraAndre">
            <VStack gap="4">
              <BodyLong>
                Legg inn annen forelders pensjoner som et årsbeløp før skatt. Legg inn pensjoner fra både private og
                offentlige ordninger.
              </BodyLong>
              <List as="ul" description="Du skal ikke legge inn">
                <List.Item>alderspensjon fra oss</List.Item>
                <List.Item>uføretrygd fra oss</List.Item>
                <List.Item>AFP i privat sektor</List.Item>
                <List.Item>AFP fra Statens pensjonskasse hvis du er under 65 år</List.Item>
              </List>
              <List as="ul" description="Du skal for eksempel legge inn">
                <List.Item>AFP offentlig</List.Item>
                <List.Item>uførepensjon fra andre enn oss</List.Item>
                <List.Item>introduksjonsstønad</List.Item>
                <List.Item>barnepensjon</List.Item>
                <List.Item>supplerende stønad</List.Item>
              </List>
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
          <ReadMore header="Pensjoner du skal legge inn">
            Legg inn den andre forelderens pensjoner fra utlandet som et årsbeløp.
          </ReadMore>
        </VStack>
      )}

      <Box padding="4" background="surface-subtle" borderRadius="large">
        <VStack gap={{ xs: '2', sm: '1' }}>
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
