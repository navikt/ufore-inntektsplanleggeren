import {BodyLong, Box, Button, Heading, HStack, Label, Link, List, VStack} from "@navikt/ds-react";
import React, {useRef} from "react";
import {ChevronDownIcon, ChevronUpIcon} from "@navikt/aksel-icons";
import {FormatKroner} from "@/components/utils/FormatKroner";
import {InitiateData} from "@/api/model/ApiRequests";
import {FormatDecimalNumber} from "@/components/utils/FormatDecimalNumber";

export function InntektsgrenseCard(props: {
    displayData: InitiateData
}) {
    const [isOpen, setIsOpen] = React.useState(false)
    const [buttonText, setButtonText] = React.useState("Vis grenser og trekkprosent")
    const inntektsgrenseCardRef = useRef<null | HTMLDivElement>(null)

    const handleButton = () => {
        setIsOpen(!isOpen)
        if (isOpen){
            inntektsgrenseCardRef!.current!.scrollIntoView()
        }
        setButtonText(isOpen ? "Vis grenser og trekkprosent" : "Skjul grenser og trekkprosent" )
    }

    return (
        <Box borderRadius="xlarge" padding="4" className="top-box" ref={inntektsgrenseCardRef}>
            <VStack gap="2">
                <Heading size="small">Inntektsgrenser og trekkprosent</Heading>
                <VStack gap="5">
                    <BodyLong style={{ whiteSpace: "pre-wrap" }}>
                        Dine inntektsgrenser sier hvor mye inntekt du kan ha før vi trekker en prosent (kompensasjonsgrad) av utbetalingen din.
                    </BodyLong>

                    {isOpen ? <VStack gap="5">
                        <section>
                            <Label as="p">Din inntektsgrense: <FormatKroner value={props.displayData.inntektsgrense}/></Label>
                            { props.displayData.hasVarigTilrettelagtArbeid ?
                                <BodyLong style={{ wordBreak:"normal"}}>
                                    Du har tiltaket <Link variant="neutral" href={"https://www.nav.no/varig-tilrettelagt-arbeid"}>Varig tilrettelagt arbeid</Link>. Bonuslønnen din kan være inntil <FormatKroner value={props.displayData.inntektsgrense}></FormatKroner> (som tilsvarer 1 G). Tjener du mer enn dette, vil du få lavere utbetaling av uføretrygd. Vi reduserer uføretrygden
                                    din av beløpet du tjener over inntektsgrensen. Beløpet opp til inntektsgrensen blir du aldri trukket for. I de fleste tilfeller vil det lønne seg å jobbe, fordi uføretrygd og inntekt er høyere enn uføretrygd alene.
                                </BodyLong> :
                                <BodyLong style={{ wordBreak:"normal"}}>
                                    Tjener du mer enn dette, vil du få lavere utbetaling av uføretrygd. Vi reduserer uføretrygden din av beløpet du tjener over inntektsgrensen.
                                    Beløpet opp til inntektsgrensen blir du aldri trukket for.
                                </BodyLong>
                            }
                        </section>

                        <section>
                            <Label as="p">Din trekkprosent (kompensasjonsgrad): <FormatDecimalNumber value={props.displayData.kompensasjonsgrad}/> prosent</Label>
                            <BodyLong style={{ wordBreak:"normal"}}>
                                Tjener du mer enn inntektsgrensen, får du lavere utbetaling av uføretrygd, ut fra din trekkprosent.
                                Vi trekker <FormatDecimalNumber value={props.displayData.kompensasjonsgrad}/> prosent kun av det du har tjent over inntektsgrensen.
                                Du vil fortsatt få utbetalt redusert uføretrygd i tillegg til lønnen din.
                            </BodyLong>
                        </section>

                        <section>
                            <Label as="p">Tjener du mer enn <FormatKroner value={props.displayData.grenseStoppAvUfoeretrygd}/> får du ikke utbetalt uføretrygd</Label>
                            <List>
                                <List.Item>Har du  inntekt på mer enn <FormatKroner value={props.displayData.grenseStoppAvUfoeretrygd}/> per år, får du ikke utbetaling av uføretrygd det aktuelle året.</List.Item>
                                <List.Item>Tjener du mer enn <FormatKroner value={props.displayData.grenseStoppAvUfoeretrygd}/> et kalenderår,  må du betale tilbake det du har fått i uføretrygd det året.</List.Item>
                                <List.Item>Du beholder likevel retten til uføretrygd. Tjener du mindre neste år, kan du igjen få utbetalt uføretrygd. </List.Item>
                            </List>
                        </section>

                        { props.displayData.hasGjenlevendeTillegg ?
                            <section>
                                <Label as="p"> Gjenlevendetillegg </Label>
                                <BodyLong style={{ wordBreak:"normal"}}>
                                    Tjener du mer enn inntektsgrensen din, reduseres også utbetalingen av gjenlevendetillegget ditt.
                                </BodyLong>
                            </section> : null
                        }

                        { props.displayData.hasBarneTilleggFellesbarn || props.displayData.hasBarnetilleggSaerkullsbarn ? //todo check if this condition is right!
                            <section>
                                <Label as="p">Barnetillegg har egne inntektsgrenser (fribeløp)</Label>
                                <BodyLong style={{ wordBreak:"normal"}}>
                                    Fribeløpet er grensen for hva foreldre kan tjene før barnetillegget blir mindre.
                                </BodyLong>
                            </section> : null
                        }

                        { props.displayData.hasBarneTilleggFellesbarn ?
                            <section>
                                <Label as="p">Fribeløp for felles barn</Label>
                                <BodyLong style={{ wordBreak:"normal"}}>
                                    Bor du sammen med barnets andre forelder, skal barnetillegget reduseres ut fra begge foreldrenes inntekt. Derfor skal du bare
                                    fylle ut den andre forelderens inntekt i inntektsplanleggeren hvis dere bor sammen.
                                    <List>
                                        <List.Item>Tjener dere tilsammen mer enn <strong><FormatKroner value={props.displayData.fribelopBarnetilleggFellesbarn}/></strong>,
                                            blir barnetillegget for barn dere har sammen mindre.</List.Item>
                                        <List.Item>Tjener dere tilsammen mer enn <strong><FormatKroner value={props.displayData.grenseStoppAvBarnetilleggFellesbarn}/></strong>,
                                            får du ikke utbetalt barnetillegget for barn dere har sammen.
                                            Får dere lavere inntekt i framtiden, kan du igjen få utbetalt barnetillegget.
                                        </List.Item>
                                    </List>
                                </BodyLong>
                            </section> : null
                        }

                        { props.displayData.hasBarnetilleggSaerkullsbarn ?
                            <section>
                                <Label as="p">Fribeløp for særkullsbarn</Label>
                                <BodyLong style={{ wordBreak:"normal"}}>
                                    Bor du ikke sammen med barnets andre forelder reduseres barnetillegget bare fra din inntekt, og du skal kun oppgi din inntekt i inntektsplanleggeren.
                                    <List>
                                        <List.Item>Tjener du mer enn <strong><FormatKroner value={props.displayData.fribelopBarnetilleggSaerkullsbarn}/>,</strong> blir barnetillegget for særkullsbarn mindre.</List.Item>
                                        <List.Item>Tjener du mer enn <strong><FormatKroner value={props.displayData.grenseStoppAvBarnetilleggSaerkullsbarn}/></strong>,
                                                får du ikke utbetalt barnetillegget for særkullsbarn. Får du lavere inntekt i framtiden, kan du igjen få utbetalt barnetillegget.
                                        </List.Item>
                                    </List>
                                </BodyLong>
                            </section> : null
                        }

                    </VStack> : null }
            </VStack>
            <HStack justify="center">
                <Button onClick={handleButton} variant="secondary-neutral" iconPosition="right" icon={isOpen ? <ChevronUpIcon aria-hidden /> : <ChevronDownIcon aria-hidden />}>{buttonText}</Button>
            </HStack>

            </VStack>
        </Box>
    )
}
