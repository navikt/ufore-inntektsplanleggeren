import {Alert, BodyLong, BodyShort, Box, FormSummary, Heading, VStack} from '@navikt/ds-react'
import {Link as RouterLink} from 'react-router-dom'
import {getFullPathForPage, PageLinks} from '@/FormContainer'
import {useContext} from 'react'
import {FormatKroner} from '@/components/utils/FormatKroner'
import {Message, PersonInntekter} from "@/api/model/ApiRequests";
import {FormStateContext} from "@/context/FormData";
import './InntektSummary.css'
import {MessageCodes} from "@/api/model/MessageCodes";

export const InntektSummaryEps = (props: { inntekt: PersonInntekter, messages: Message[] | undefined, inntektSum: number}) => {
    const { selectedYear } = useContext(FormStateContext)

    const arbeidsinntektMessage = props.messages?.find(message => message.messageCode === MessageCodes.ARBEIDSINNTEKT_GIVEN_SMALLER_THAN_HITTIL_I_AAR && message.metadata["AFFECTED_FIELD"] === "ARBEIDSINNTEKT_EPS")
    const andreYttelserMessage = props.messages?.find(message => message.messageCode === MessageCodes.ANDRE_YTELSER_SMALLER_THAN_HITTIL_I_AAR && message.metadata["AFFECTED_FIELD"] === "ANDRE_YTELSER_EPS")


    return (
      <FormSummary>
        <FormSummary.Header>
          <FormSummary.Heading level="3">Annen forelders forventede inntekt {selectedYear}</FormSummary.Heading>
          <FormSummary.EditLink as={RouterLink} to={getFullPathForPage(PageLinks.FORVENTET_INNTEKT, "eps-inntekt")}/>
        </FormSummary.Header>
        <FormSummary.Answers>
          {props.inntekt.arbeidsinntekt !== null && (
              <FormSummary.Answer>
                <FormSummary.Label>Lønn og pensjonsgivende ytelser</FormSummary.Label>
                <FormSummary.Value>
                    {arbeidsinntektMessage !== undefined &&
                        <Alert variant="warning">
                            <Heading level="4" size="small">Ojsann! Dette beløpet var litt lavt!</Heading>
                            <BodyLong>Vi har registrert at barnets andre forelder allerede har fått dette beløpet i inntekt hittil i år. Du må sjekke at beløpet du vil sende inn stemmer. Kan noe av inntekten holdes utenfor etteroppgjøret,
                                kan det være riktig at beløpet du sender inn er lavere. Den andre forelderen kan se  inntekt som er registrert hittil i år hos Skatteetaten.</BodyLong>
                        </Alert>
                    }
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
                    {andreYttelserMessage !== undefined &&
                        <Alert variant="warning">
                            <Heading level="4" size="small">Ojsann! Dette beløpet var litt lavt!</Heading>
                            <BodyLong>Vi har registrert at barnets andre forelder allerede har fått dette i pensjoner hittil i år. Du må sjekke at beløpet du vil sende inn stemmer. Kan noe av pensjonen holdes utenfor etteroppgjøret,
                                kan det være riktig at beløpet du sender inn er lavere.  Den andre forelderen kan se  inntekt som er registrert hittil i år hos Skatteetaten.</BodyLong>
                        </Alert>
                    }
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
                <Box padding="4" background="surface-subtle" borderRadius="large">
                    <VStack>
                        <Heading size="small">Annen forelder sin samlede inntekt i {selectedYear}: </Heading>
                        <BodyShort className="sum"> <FormatKroner value={props.inntektSum}/> før skatt </BodyShort>
                    </VStack>
                </Box>
            </FormSummary.Answer>
        </FormSummary.Answers>
      </FormSummary>
  )
}
