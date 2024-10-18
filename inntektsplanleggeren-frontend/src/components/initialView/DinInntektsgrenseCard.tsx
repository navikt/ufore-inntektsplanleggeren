import {BodyLong, ExpansionCard, Label} from "@navikt/ds-react";
import {DisplayData} from "@/api/apiFetching";
import {Link} from "react-router-dom";

//todo see if it is possible to reduce the number of versions

export function InntektsgrenseCard(props: {
    displayData: DisplayData
}) {
    return (<div>
            <ExpansionCard aria-label="Demo med bare tittel">
                <ExpansionCard.Header>
                    <ExpansionCard.Title>Din inntektsgrense med mer</ExpansionCard.Title>
                </ExpansionCard.Header>
                <ExpansionCard.Content>
                    <BodyLong spacing>
                        Dette er grensene som avgjør hvor mye inntekt du kan ha før utbetalingen av uføretrygden din
                        blir justert ned. Det vil likevel lønne seg å jobbe, fordi uføretrygd og inntekt er høyere enn
                        uføretrygd alene.
                    </BodyLong>

                    <Label as="p">Din inntektsgrense: {props.displayData.inntektsgrense}kr</Label>
                    {props.displayData.hasVarigTilrettelagtArbeid ?
                        <BodyLong spacing>
                            Du har tiltaket <Link to={"nav.no"}>Varig tilrettelagt arbeid</Link>. Bonuslønnen din kan være inntil 124 028 kroner, som tilsvarer grunnbeløpet
                            i folketrygden. Hvis du får høyere inntekt enn dette, begynner vi å trekke en prosent av uføretrygd tilsvarende det beløpet
                            du tjener over inntektsgrensen. Beløpet opp til inntektsgrensen blir du aldri trukket for.
                        </BodyLong> :
                        <BodyLong spacing>
                            Hvis du får høyere inntekt enn inntektsgrensen, begynner vi å trekke en prosent av uføretrygd
                            tilsvarende det beløpet du tjener over inntektsgrensen. Beløpet opp til inntektsgrensen blir du
                            aldri trukket for.
                        </BodyLong>
                    }

                    <Label as="p">Din kompensasjonsgrad: {props.displayData.kompensasjonsgrad}%</Label>
                    <BodyLong spacing>
                        Det betyr at hvis du tjener 1000 kr over inntektsgrensen, trekker vi 70% av 1000 kr fra
                        uføretrygden din, det vil si at vi trekker 700 kr. Du vil fortsatt få 300 kr uføretrygd i
                        tillegg til lønnen din.
                    </BodyLong>

                    <Label as="p">Inntekt som ikke gir deg utbetaling av uføretrygd det året: {props.displayData.grenseStoppAvUfoeretrygd}kr</Label>
                    <BodyLong spacing>
                        Hvis du tjener over denne summen får du ikke uføretrygd det aktuelle året. Neste vil du få
                        uføretrygd igjen, dersom du ikke tjener for mye også det året.
                    </BodyLong>

                    { props.displayData.hasBarneTilleggFellesbarn || props.displayData.hasBarnetilleggSaerkullsbarn ? //todo check if this condition is right!
                        <div>
                            <Label as="p"> Barnetillegg har egne grenser </Label>
                            <Link to={"nav.no"}> Les om inntektsgrenser for barnetillegg.</Link>
                            <BodyLong spacing>
                                Hvis du tjener over denne summen får du ikke uføretrygd det aktuelle året. Neste vil du
                                få uføretrygd igjen, dersom du ikke tjener for mye også det året.
                            </BodyLong>
                        </div> : <></>
                    }

                    { props.displayData.hasGjenlevendeTillegg ?
                        <div>
                            <Label as="p"> Gjenlevendetillegg </Label>
                            <BodyLong spacing>
                                Gjenlevendetillegget reduseres i samme forhold som uføretrygden om du har inntekt over inntektsgrensen.
                            </BodyLong>
                        </div> : <></>
                    }

                </ExpansionCard.Content>
                </ExpansionCard>
        </div>
    )
}
