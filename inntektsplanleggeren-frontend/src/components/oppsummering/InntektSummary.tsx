import { BodyShort, Box, FormSummary, Heading, VStack } from '@navikt/ds-react'
import { useContext } from 'react'
import { Link as RouterLink } from 'react-router-dom'
import type { PersonInntekter } from '@/api/model/ApiRequests'
import { FormatKroner } from '@/components/utils/FormatKroner'
import { FormStateContext } from '@/context/FormData'
import { getFullPathForPage, PageLinks } from '@/FormContainer'
import './InntektSummary.css'

export const InntektSummary = (props: { inntekt: PersonInntekter; inntektSum: number; type: 'bruker' | 'eps' }) => {
    const { selectedYear } = useContext(FormStateContext)
    const href = props.type === 'bruker' ? 'bruker-inntekt' : 'eps-inntekt'

    return (
        <FormSummary>
            <FormSummary.Header>
                <FormSummary.Heading level="3">
                    {props.type === 'bruker' ? `Din inntekt ${selectedYear}` : `Annen forelders forventede inntekt ${selectedYear}`}
                </FormSummary.Heading>
                <FormSummary.EditLink as={RouterLink} to={getFullPathForPage(PageLinks.FORVENTET_INNTEKT, href)} />
            </FormSummary.Header>
            <FormSummary.Answers>
                {props.inntekt.arbeidsinntekt !== null && (
                    <FormSummary.Answer>
                        <FormSummary.Label>Lønn og pensjonsgivende ytelser</FormSummary.Label>
                        <FormSummary.Value>
                            <FormatKroner value={props.inntekt.arbeidsinntekt} />
                        </FormSummary.Value>
                    </FormSummary.Answer>
                )}
                {props.inntekt.naeringsinntekt !== null && (
                    <FormSummary.Answer>
                        <FormSummary.Label>Næringsinntekt</FormSummary.Label>
                        <FormSummary.Value>
                            <FormatKroner value={props.inntekt.naeringsinntekt} />
                        </FormSummary.Value>
                    </FormSummary.Answer>
                )}
                {props.inntekt.inntektUtland !== null && (
                    <FormSummary.Answer>
                        <FormSummary.Label>Inntekt fra utlandet</FormSummary.Label>
                        <FormSummary.Value>
                            <FormatKroner value={props.inntekt.inntektUtland} />
                        </FormSummary.Value>
                    </FormSummary.Answer>
                )}
                {props.inntekt.andrePensjonsgivendeYtelser !== null && (
                    <FormSummary.Answer>
                        <FormSummary.Label>Uførepensjon og pensjoner fra andre enn Nav </FormSummary.Label>
                        <FormSummary.Value>
                            <FormatKroner value={props.inntekt.andrePensjonsgivendeYtelser} />
                        </FormSummary.Value>
                    </FormSummary.Answer>
                )}
                {props.inntekt.pensjonUtland !== null && (
                    <FormSummary.Answer>
                        <FormSummary.Label>Pensjoner fra utlandet</FormSummary.Label>
                        <FormSummary.Value>
                            <FormatKroner value={props.inntekt.pensjonUtland} />
                        </FormSummary.Value>
                    </FormSummary.Answer>
                )}
                <FormSummary.Answer>
                    <Box padding="space-16" background="neutral-soft" borderRadius="8">
                        <VStack gap={{ xs: 'space-8', sm: 'space-4' }}>
                            <Heading level="4" size="small">
                                {props.type === 'bruker'
                                    ? `Din samlede forventede inntekt i ${selectedYear}:`
                                    : `Annen forelder sin samlede inntekt i ${selectedYear}:`}
                            </Heading>
                            <BodyShort className="sum">
                                <FormatKroner value={props.inntektSum} /> før skatt
                            </BodyShort>
                        </VStack>
                    </Box>
                </FormSummary.Answer>
            </FormSummary.Answers>
        </FormSummary>
    )
}
