import { Message } from '@/api/model/ApiRequests'
import { Alert, VStack } from '@navikt/ds-react'
import { MessageCodes } from '@/api/model/MessageCodes'

export function BeregningWarnings(props: { messages: Message[] }) {
  return (
    <VStack gap="3">
      {props.messages.some((message) => message.messageCode === MessageCodes.OPPGITT_INNTEKT_OVER_INNTEKTSTAK) && (
        <Alert variant="warning">
          Inntekten du har lagt inn er høyere enn 80 prosent av inntekten du hadde før du ble ufør. Derfor får du ikke
          utbetalt uføretrygd resten av året. Har du fortsatt fått utbetalt for mye når året er slutt, kan du få et
          etteroppgjør hvor du må betale tilbake.
        </Alert>
      )}

      {props.messages.some(
        (message) => message.messageCode === MessageCodes.OPPGITT_INNTEKT_GIVES_LOWER_UFORETRYGD_THAN_ALREADY_UTBETALT
      ) && (
        <Alert variant="warning">
          Inntekten du har lagt inn, er høyere enn inntekten vi har brukt til å beregne uføretrygden din. Derfor kan det
          være at du ikke får utbetalt uføretrygd resten av året. Får du barnetillegg, kan det hende at du ikke får det
          utbetalt resten av året. Har du fortsatt fått for mye utbetalt ved slutten av året, kan du få et etteroppgjør
          hvor du må betale tilbake.
        </Alert>
      )}

      {props.messages.some(
        (message) => message.messageCode === MessageCodes.OPPGITT_INNTEKT_GIVES_MORE_UFORETRYGD_THAN_ALREADY_UTBETALT
      ) && (
        <Alert variant="info">
          Inntekten du har lagt inn, er lavere enn inntekten vi har brukt til å beregne uføretrygden din.
            Derfor får du ikke trekk i uføretrygden din resten av året.
          Har du fått utbetalt for lite uføretrygd ved slutten av året, kan du få tilbake penger i etteroppgjøret.
        </Alert>
      )}

      {props.messages.some(
        (message) =>
          message.messageCode === MessageCodes.ONE_OR_MORE_INNTEKT_HAS_STATUS_REGISTRERT ||
          message.messageCode === MessageCodes.OPEN_INNTEKTSENDRING_KRAV
      ) && (
        <Alert variant="warning">
          Du har tidligere meldt inn en inntektsendring som ikke er behandet enda. Derfor ser du ikke oppdatert inntekt
          og uføretrygd for i dag.
        </Alert>
      )}
    </VStack>
  )
}
