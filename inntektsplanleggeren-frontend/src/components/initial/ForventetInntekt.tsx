import { BodyLong, Box, Heading, VStack } from '@navikt/ds-react'
import { FormatKroner } from '@/components/utils/FormatKroner'
import {InitiateData} from '@/api/model/ApiRequests'

interface Props {
  data: InitiateData
}

export default function ForventetInntekt({ data }: Props) {
    const forventetInntektMap = new Map(Object.entries(data.forventetInntekt))
    const forventetInntektAnnenForelderMap = new Map(Object.entries(data.forventetInntektAnnenForelder))

    const ForventetInntektInnhold = ({ år }: { år: string }) => {
      const forventetInntekt = forventetInntektMap.get(år)
      const expectedIncomeAnnenForelder = forventetInntektAnnenForelderMap.get(år)

      return (<Box borderRadius="12" padding="space-16" background="accent-soft">
        <VStack gap="space-28">
          <Heading level="2" size="small">
            Registrert forventet inntekt for {år}
          </Heading>
          <BodyLong>
            Din registrerte forventede inntekt:{' '}
            {forventetInntekt !== null && forventetInntekt !== undefined ? (
                <strong>
                  <FormatKroner value={forventetInntekt} />
                </strong>
            ) : (
                <strong>Ingen registrert forventet inntekt funnet</strong>
            )}
          </BodyLong>
          {data.hasBarneTilleggFellesbarn ? (
              <BodyLong>
                Annen forelder du bor med sin forventede inntekt:{' '}
                {expectedIncomeAnnenForelder !== null && expectedIncomeAnnenForelder !== undefined ? (
                    <strong>
                      <FormatKroner value={expectedIncomeAnnenForelder} />
                    </strong>
                ) : (
                    <strong>Ingen registrert forventet inntekt funnet</strong>
                )}
              </BodyLong>
          ) : (
              <></>
          )}
          <BodyLong>
            Din inntektsgrense:{' '}
            <strong>
              <FormatKroner value={data.inntektsgrense} />
            </strong>
          </BodyLong>
          <BodyLong>
            Reduksjonsprosent: <strong>{data.kompensasjonsgrad} prosent</strong>
          </BodyLong>
          <BodyLong>
            Inntektstak:{' '}
            <strong>
              <FormatKroner value={data.grenseStoppAvUfoeretrygd} />
            </strong>
          </BodyLong>
        </VStack>
      </Box>)
    }


    return <VStack gap={'space-32'}>
      {Array.from(forventetInntektMap.keys()).map((year) => (
          <ForventetInntektInnhold key={year} år={year}/>
      ))}
    </VStack>

}
