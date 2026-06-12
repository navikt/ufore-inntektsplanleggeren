import { ChevronDownIcon, ChevronUpIcon } from '@navikt/aksel-icons'
import { BodyLong, Box, Button, Heading, HStack, Link, List, VStack } from '@navikt/ds-react'
import React, { useRef } from 'react'
import type { InitiateData } from '@/api/model/ApiRequests'
import { FormatDecimalNumber } from '@/components/utils/FormatDecimalNumber'
import { FormatKroner } from '@/components/utils/FormatKroner'

export function InntektsgrenseCard(props: { displayData: InitiateData }) {
    const [isOpen, setIsOpen] = React.useState(false)
    const [buttonText, setButtonText] = React.useState('Vis grenser og trekkprosent')
    const inntektsgrenseCardRef = useRef<null | HTMLDivElement>(null)

    const handleButton = () => {
        setIsOpen(!isOpen)
        if (isOpen) {
            inntektsgrenseCardRef!.current!.scrollIntoView()
        }
        setButtonText(isOpen ? 'Vis grenser og trekkprosent' : 'Skjul grenser og trekkprosent')
    }

    return (
        <Box borderRadius="12" padding="space-16" background="accent-soft" ref={inntektsgrenseCardRef}>
            <VStack gap="space-8">
                <VStack gap="space-8" style={{ marginBottom: '20px' }}>
                    <Heading level="2" size="small">
                        Inntektsgrenser og trekkprosent
                    </Heading>
                    <VStack gap="space-20">
                        <BodyLong style={{ whiteSpace: 'pre-wrap' }}>
                            Dine inntektsgrenser sier hvor mye inntekt du kan ha før vi trekker en prosent (kompensasjonsgrad) av utbetalingen din.
                        </BodyLong>

                        {isOpen ? (
                            <VStack gap="space-20">
                                <VStack>
                                    <Heading size="xsmall" level="3">
                                        Din inntektsgrense: <FormatKroner value={props.displayData.inntektsgrense} />
                                    </Heading>
                                    {props.displayData.hasVarigTilrettelagtArbeid ? (
                                        <BodyLong style={{ wordBreak: 'normal' }}>
                                            Du har tiltaket{' '}
                                            <Link data-color="neutral" href={'https://www.nav.no/varig-tilrettelagt-arbeid'}>
                                                Varig tilrettelagt arbeid
                                            </Link>
                                            . Bonuslønnen din kan være inntil <FormatKroner value={props.displayData.inntektsgrense}></FormatKroner> (som
                                            tilsvarer 1 G). Tjener du mer enn dette, vil du få lavere utbetaling av uføretrygd. Vi reduserer uføretrygden din av
                                            beløpet du tjener over inntektsgrensen. Beløpet opp til inntektsgrensen blir du aldri trukket for. I de fleste
                                            tilfeller vil det lønne seg å jobbe, fordi uføretrygd og inntekt er høyere enn uføretrygd alene.
                                        </BodyLong>
                                    ) : (
                                        <BodyLong style={{ wordBreak: 'normal' }}>
                                            Tjener du mer enn dette, vil du få lavere utbetaling av uføretrygd. Vi reduserer uføretrygden din av beløpet du
                                            tjener over inntektsgrensen. Beløpet opp til inntektsgrensen blir du aldri trukket for.
                                        </BodyLong>
                                    )}
                                </VStack>
                                <VStack>
                                    <Heading size="xsmall" level="3">
                                        Din trekkprosent (kompensasjonsgrad): <FormatDecimalNumber value={props.displayData.kompensasjonsgrad} /> prosent
                                    </Heading>
                                    <BodyLong style={{ wordBreak: 'normal' }}>
                                        Tjener du mer enn inntektsgrensen, får du lavere utbetaling av uføretrygd, ut fra din trekkprosent. Vi trekker{' '}
                                        <FormatDecimalNumber value={props.displayData.kompensasjonsgrad} /> prosent kun av det du har tjent over
                                        inntektsgrensen. Du vil fortsatt få utbetalt redusert uføretrygd i tillegg til lønnen din.
                                    </BodyLong>
                                </VStack>

                                <VStack>
                                    <Heading size="xsmall" level="3">
                                        Tjener du mer enn <FormatKroner value={props.displayData.grenseStoppAvUfoeretrygd} /> får du ikke utbetalt uføretrygd
                                    </Heading>
                                    <List>
                                        <List.Item>
                                            Har du inntekt på mer enn <FormatKroner value={props.displayData.grenseStoppAvUfoeretrygd} /> per år, får du ikke
                                            utbetaling av uføretrygd det aktuelle året.
                                        </List.Item>
                                        <List.Item>
                                            Tjener du mer enn <FormatKroner value={props.displayData.grenseStoppAvUfoeretrygd} /> et kalenderår, må du betale
                                            tilbake det du har fått i uføretrygd det året.
                                        </List.Item>
                                        <List.Item>
                                            Du beholder likevel retten til uføretrygd. Tjener du mindre neste år, kan du igjen få utbetalt uføretrygd.{' '}
                                        </List.Item>
                                    </List>
                                </VStack>

                                {props.displayData.hasGjenlevendeTillegg ? (
                                    <VStack>
                                        <Heading size="xsmall" level="3">
                                            Gjenlevendetillegg
                                        </Heading>
                                        <BodyLong style={{ wordBreak: 'normal' }}>
                                            Tjener du mer enn inntektsgrensen din, reduseres også utbetalingen av gjenlevendetillegget ditt.
                                        </BodyLong>
                                    </VStack>
                                ) : null}

                                {props.displayData.hasBarneTilleggFellesbarn || props.displayData.hasBarnetilleggSaerkullsbarn ? (
                                    <VStack>
                                        <Heading size="xsmall" level="3">
                                            Barnetillegg
                                        </Heading>
                                        <BodyLong style={{ wordBreak: 'normal' }}>
                                            Inntekten din har betydning for hvor mye du får utbetalt i barnetillegg. For barn som bor med begge sine foreldre,
                                            bruker vi begge foreldrenes inntekt når vi beregner størrelsen på barnetillegget.
                                            <Link data-color="neutral" href={'https://www.nav.no/uforetrygd#tillegg'}>
                                                Les mer om barnetillegg til uføretrygden.
                                            </Link>
                                        </BodyLong>
                                    </VStack>
                                ) : null}
                            </VStack>
                        ) : null}
                    </VStack>
                </VStack>
                <HStack justify="center">
                    <Button
                        data-color="neutral"
                        onClick={handleButton}
                        variant="secondary"
                        iconPosition="right"
                        icon={isOpen ? <ChevronUpIcon aria-hidden /> : <ChevronDownIcon aria-hidden />}
                    >
                        {buttonText}
                    </Button>
                </HStack>
            </VStack>
        </Box>
    )
}
