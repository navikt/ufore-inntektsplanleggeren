import { PersonInntekter } from '@/api/model/ApiRequests'
import { BodyShort, VStack } from '@navikt/ds-react'
import { FormatKroner } from '@/components/utils/FormatKroner'

interface IProps {
  inntekter: PersonInntekter
  sumOverrideText?: String
}

export const InputSummary: React.FC<IProps> = (props) => {
  return (
    <VStack gap="1">
      <BodyShort>
        Lønn og pensjonsgivende ytelser: <FormatKroner value={props.inntekter.arbeidsinntekt ?? 0} />
      </BodyShort>
      <BodyShort>
        Næringsinntekt: <FormatKroner value={props.inntekter.naeringsinntekt ?? 0} />
      </BodyShort>
      <BodyShort>
        Inntekt fra utlandet: <FormatKroner value={props.inntekter.inntektUtland ?? 0} />
      </BodyShort>
      <BodyShort>
        Uførepensjon og pensjoner fra andre enn Nav:{' '}
        <FormatKroner value={props.inntekter.andrePensjonsgivendeYtelser ?? 0} />
      </BodyShort>
      <BodyShort>
        Pensjoner fra utlandet: <FormatKroner value={props.inntekter.pensjonUtland ?? 0} />
      </BodyShort>
      <BodyShort>
        <strong>
          {props.sumOverrideText ?? 'Sum forventet inntekt'}:{' '}
          <FormatKroner
            value={
              (props.inntekter.arbeidsinntekt ?? 0) +
              (props.inntekter.naeringsinntekt ?? 0) +
              (props.inntekter.inntektUtland ?? 0) +
              (props.inntekter.andrePensjonsgivendeYtelser ?? 0) +
              (props.inntekter.pensjonUtland ?? 0)
            }
          />
        </strong>
      </BodyShort>
    </VStack>
  )
}
