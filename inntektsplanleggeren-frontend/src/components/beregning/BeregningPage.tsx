import { Alert, BodyLong, BodyShort, Heading, HelpText, HStack, InfoCard, Link, ReadMore, VStack } from '@navikt/ds-react'
import { type FormEvent, type MouseEvent, useContext, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { MessageCodes, MessageTypes } from '@/api/model/MessageCodes'
import { BeregningWarnings } from '@/components/beregning/BeregningWarnings'
import { Graph } from '@/components/beregning/Graph'
import { InputSummary } from '@/components/beregning/InputSummary'
import { SimulationTable } from '@/components/beregning/SimulationTable'
import { ErrorView } from '@/components/common/Error'
import { FormatKroner } from '@/components/utils/FormatKroner'
import { DataContext } from '@/context/DataContextProvider'
import { FormStateContext } from '@/context/FormData'
import { getFullPathForPage, PageLinks } from '@/FormContainer'
import './BeregningPage.css'
import { ExclamationmarkTriangleFillIcon } from '@navikt/aksel-icons'
import Knapperad from '@/components/common/Knapperad'
import { useToggle } from '@/hooks/useToggle'

export const BeregningPage = () => {
    const { selectedYear, setFormStep, brukerinntekt, annenForelderInntekt } = useContext(FormStateContext)
    const { simulationResponse, errorMessage } = useContext(DataContext)
    const navigate = useNavigate()
    const skalViseOmregningVarsel = useToggle('ufore.omregning-varsel')

    useEffect(() => {
        setFormStep(2)
    }, [setFormStep])

    const handleSubmit = async (e: MouseEvent | FormEvent) => {
        e.preventDefault()
        navigate(getFullPathForPage(PageLinks.OPPSUMMERING))
    }

    const showSimulering = !simulationResponse?.messages.some(
        (message) =>
            message.messageCode === MessageCodes.FAKTOROMREGNET_ELLER_MANUELT_OVERSTYRT || message.messageCode === MessageCodes.SIMULERING_CONTAINS_MOTREGNING
    )

    if (errorMessage) {
        return <ErrorView message={errorMessage} />
    }

    if (simulationResponse?.messages.some((message) => message.type === MessageTypes.ERROR)) {
        return <ErrorView message={null} />
    }

    if (simulationResponse?.result)
        return (
            <VStack gap="space-32">
                {skalViseOmregningVarsel && (
                    <InfoCard data-color="warning">
                        <InfoCard.Message icon={<ExclamationmarkTriangleFillIcon aria-hidden color="#C95100" />}>
                            <Heading size="small" level="2" spacing>
                                Viktig informasjon om beregningene dine
                            </Heading>
                            <BodyShort spacing>
                                Melder du fra om inntekt før 1. oktober, vil beregningen du ser her ikke ta hensyn til de nye reglene om økt bunnfradrag. Du kan
                                trygt melde fra likevel. Vi gjør en ny beregning av uføretrygden din for oktober, slik at utbetalingen din blir riktig.
                            </BodyShort>
                            <BodyShort spacing>Oppdatert informasjon om nytt bunnfradrag vil komme innen oktober.</BodyShort>
                        </InfoCard.Message>
                    </InfoCard>
                )}
                {simulationResponse.messages.some((message) => message.messageCode === MessageCodes.USER_HAS_NO_LOPENDE_VEDTAK_YET) ? (
                    <Alert variant="warning">
                        Du kan ikke bruke inntektsplanleggeren ennå. Din inntekt kan registreres her fra måneden før din første utbetaling av uføretrygd.
                    </Alert>
                ) : null}
                <section aria-label={'Din inntekt og uføretrygd før skatt i ' + selectedYear}>
                    <Heading level="3" size="medium" style={{ marginBottom: '20px' }}>
                        Din inntekt og uføretrygd før skatt i {selectedYear}
                    </Heading>
                    <ReadMore header="Inntekt du har lagt inn">
                        <VStack gap="space-28">
                            <VStack>
                                <Heading level="4" size="small">
                                    Din forventede inntekt i {selectedYear}
                                </Heading>
                                <InputSummary inntekter={brukerinntekt}></InputSummary>
                            </VStack>
                            {annenForelderInntekt ? (
                                <VStack>
                                    <Heading level="4" size="small">
                                        Annen forelders forventede inntekt i {selectedYear}
                                    </Heading>
                                    <InputSummary inntekter={annenForelderInntekt} sumOverrideText="Sum annen forelders inntekt"></InputSummary>
                                </VStack>
                            ) : null}
                        </VStack>
                    </ReadMore>
                </section>
                <BeregningWarnings messages={simulationResponse.messages} />
                {showSimulering ? (
                    <>
                        {!simulationResponse?.messages.some((message) => message.messageCode === MessageCodes.SIMULERING_CONTAINS_OPPHORTE_YTELSER) && (
                            <>
                                <section aria-hidden={true}>
                                    <Heading level="4" size="medium">
                                        Oversikt i graf
                                    </Heading>
                                    <div style={{ marginTop: '10px' }}>
                                        <Graph simulationResult={simulationResponse?.result} />
                                    </div>
                                </section>

                                <section aria-label="Oversikt i tabell">
                                    <Heading level="4" size="medium">
                                        <HStack>
                                            Oversikt i tabell
                                            <HelpText id="helpbox">
                                                <BodyLong>"I dag" viser årlig beløp hentet fra vedtaket som gjelder nå.</BodyLong>
                                                <BodyLong>"Med dine endringer" viser årlig beløp med endringene du nå har lagt inn.</BodyLong>
                                            </HelpText>
                                        </HStack>
                                    </Heading>

                                    {simulationResponse?.result && <SimulationTable simulationResult={simulationResponse.result}></SimulationTable>}
                                </section>
                            </>
                        )}
                        <section aria-label="Månedlig utbetaling">
                            <BodyLong>
                                <strong>
                                    Månedlig utbetaling av uføretrygd med dine endringer, før skatt:{' '}
                                    <FormatKroner value={simulationResponse?.result.sum.monthly.after ?? 0} />
                                </strong>
                            </BodyLong>
                            <ReadMore header="Månedsbeløp spesifisert">
                                <BodyLong>
                                    {simulationResponse?.result?.gjenlevendetillegg ? 'Uføretrygd inkludert gjenlevendetillegg: ' : 'Uføretrygd: '}
                                    <FormatKroner
                                        value={
                                            (simulationResponse?.result?.uforetrygd.monthly.after ?? 0) +
                                            (simulationResponse?.result.gjenlevendetillegg?.monthly.after ?? 0)
                                        }
                                    />
                                </BodyLong>
                                {simulationResponse?.result.barnetilleggFellesbarn !== null ? (
                                    <BodyLong>
                                        Barnetillegg for fellesbarn:{' '}
                                        <FormatKroner value={simulationResponse?.result.barnetilleggFellesbarn.monthly.after ?? 0} />
                                    </BodyLong>
                                ) : null}
                                {simulationResponse?.result.barnetilleggSaerkullsbarn !== null ? (
                                    <BodyLong>
                                        Barnetillegg for særkullsbarn:{' '}
                                        <FormatKroner value={simulationResponse?.result.barnetilleggSaerkullsbarn.monthly.after ?? 0} />
                                    </BodyLong>
                                ) : null}
                                {simulationResponse?.result.sum.monthly.after !== null ? (
                                    <BodyLong>
                                        <strong>
                                            Totalt per måned: <FormatKroner value={simulationResponse?.result.sum.monthly.after} />
                                        </strong>
                                    </BodyLong>
                                ) : null}
                            </ReadMore>
                        </section>
                        {simulationResponse?.messages.some((message) => message.messageCode === MessageCodes.SIMULERING_CONTAINS_OPPHORTE_YTELSER) && (
                            <Alert variant="info">Total årlig beregning vil først være mulig å se fra neste år.</Alert>
                        )}
                    </>
                ) : (
                    <Alert variant="warning">
                        Vi kan dessverre ikke vise hvordan den nye inntekten din vil påvirke utbetalingen din av uføretrygd. Du kan likevel sende inn din
                        inntektsendring.
                    </Alert>
                )}
                <BodyLong>
                    <strong>
                        Har du spørsmål?{' '}
                        <Link href={PageLinks.KONTAKT} target="_blank">
                            Kontakt oss (åpnes i ny fane)
                        </Link>
                    </strong>
                </BodyLong>
                <Knapperad handleSubmit={handleSubmit} tilbakePageLink={PageLinks.FORVENTET_INNTEKT} />
            </VStack>
        )
}
