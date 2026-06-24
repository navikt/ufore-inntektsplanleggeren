import { Accordion, BodyLong, BodyShort, Box, GuidePanel, Heading, Link, List, VStack } from '@navikt/ds-react'
import React from 'react'
import type { InitiateData } from '@/api/model/ApiRequests'
import { YearView } from '@/components/initial/YearView'
import { FormatKroner } from '@/components/utils/FormatKroner'

interface Props {
    data: InitiateData
    handleButtonClick: (year: number, previousYear: number | null) => Promise<void>
    isLoading: boolean
}

export default function StartsideInnhold({ data, handleButtonClick, isLoading }: Props) {
    const forventetInntektMap = new Map(Object.entries(data.forventetInntekt))
    const forventetInntektAnnenForelderMap = new Map(Object.entries(data.forventetInntektAnnenForelder))

    const ExpectedIncome = ({ year }: { year: string }) => {
        const forventetInntekt = forventetInntektMap.get(year)
        const expectedIncomeAnnenForelder = forventetInntektAnnenForelderMap.get(year)
        return (
            <Box key={year} borderRadius="12" padding="space-16" background="accent-soft">
                <VStack gap="space-28">
                    <Heading level="2" size="small">
                        Registrert forventet inntekt for {year}
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
            </Box>
        )
    }

    return (
        <>
            <section aria-label={'Greit å vite'}>
                <GuidePanel poster>
                    <Heading size="medium" level="2" spacing>
                        Greit å vite
                    </Heading>
                    <BodyShort spacing>
                        I inntektsplanleggeren kan du se hvordan inntekt påvirker uføretrygden din, og melde inn inntekt til oss. Vi bruker inntekten du melder
                        inn til å beregne riktig utbetaling av uføretrygd og eventuelle tillegg.
                    </BodyShort>
                    <BodyShort spacing>Du kan melde fra om inntekt så ofte du trenger.</BodyShort>
                    <BodyShort>
                        Dine opplysninger lagres dessverre ikke hvis du logger ut av innteksplanleggeren, eller tar en lang pause. Vi beklager for dette.
                    </BodyShort>
                </GuidePanel>
            </section>
            <section>
                {data?.aktuelleAar?.length > 0 && (
                    <YearView
                        availableYears={data.aktuelleAar}
                        anotherAvalableYear={data.annetRelevantAar}
                        handleSubmit={handleButtonClick}
                        isLoading={isLoading}
                    ></YearView>
                )}
            </section>
            <section aria-label={'Dine tall'}>
                <VStack gap={'space-32'}>
                    {Array.from(forventetInntektMap.keys()).map((year) => (
                        <ExpectedIncome key={year} year={year} />
                    ))}
                </VStack>
            </section>
            <Accordion>
                <Accordion.Item>
                    <Accordion.Header>Dette bør du melde fra om</Accordion.Header>
                    <Accordion.Content>
                        <List>
                            <List.Item>Hvis du har barnetillegg og bor sammen med barnets andre forelder, skal du også oppgi forelderens inntekt.</List.Item>
                            <List.Item>Du kan melde fra om forventet inntekt for neste år fra oktober i år.</List.Item>
                            <List.Item>Har du ingen endring i inntekten din til neste år, bør du likevel melde inn forventet inntekt.</List.Item>
                            <List.Item>
                                Melder du ikke fra om forventet inntekt til neste år, vil vi bruke inntekten du har oppgitt i år og justere den ved årsskiftet.
                            </List.Item>
                        </List>
                    </Accordion.Content>
                </Accordion.Item>
                <Accordion.Item>
                    <Accordion.Header>Usikker på hva du kommer til å tjene?</Accordion.Header>
                    <Accordion.Content>
                        <VStack gap="space-32">
                            <BodyShort>
                                Har du variabel inntekt, kan det være vanskelig å vite hva du kommer til å tjene fremover. Vi stoler på at du melder fra til oss
                                så godt du kan.
                            </BodyShort>
                            <BodyShort>
                                Ser du at inntekten din blir annerledes enn det du tidligere har meldt inn, bør du melde fra til oss så fort som mulig. Du kan
                                bruke inntektsplanleggeren så ofte du trenger.
                            </BodyShort>
                            {/* TODO: hvilken lenke? */}
                            <Link>
                                På nav.no finner du mer informasjon om hvordan du legger inn riktig inntekt, og eksempler på beregning når inntekten din endrer
                                seg.
                            </Link>
                        </VStack>
                    </Accordion.Content>
                </Accordion.Item>
            </Accordion>
        </>
    )
}
