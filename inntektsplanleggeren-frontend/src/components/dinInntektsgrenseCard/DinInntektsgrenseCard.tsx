import {BodyLong, ExpansionCard, Label} from "@navikt/ds-react";

//todo see if it is possible to reduce the number of versions

export const UforetrydgInBarnetilleggOgGjenlevendetillegg = (intektsgrense: string, kompensasjonsgrad: string, maksInntektsgrense: string) => (<>
    <ExpansionCard aria-label="Demo med bare tittel">
        <ExpansionCard.Header>
            <ExpansionCard.Title>Din inntektsgrense med mer</ExpansionCard.Title>
        </ExpansionCard.Header>
        <ExpansionCard.Content>
            <BodyLong spacing>
                Dette er grensene som avgjør hvor mye inntekt du kan ha før utbetalingen av uføretrygden din blir justert ned. Det vil likevel lønne seg å jobbe, fordi uføretrygd og inntekt er høyere enn uføretrygd alene.
            </BodyLong>
            <Label as="p" spacing>
                Din inntektsgrense: {intektsgrense}
            </Label>
            <BodyLong spacing>
                Hvis du får høyere inntekt enn inntektsgrensen, begynner vi å trekke en prosent av uføretrygd tilsvarende det beløpet du tjener over inntektsgrensen. Beløpet opp til inntektsgrensen blir du aldri trukket for.
            </BodyLong>
            <Label as="p" spacing>
                Din kompensasjonsgrad: {kompensasjonsgrad}
            </Label>
            <BodyLong spacing>
                Det betyr at hvis du tjener 1000 kr over inntektsgrensen, trekker vi 70% av 1000 kr fra uføretrygden din, det vil si at vi trekker 700 kr. Du vil fortsatt få 300 kr uføretrygd i tillegg til lønnen din.
            </BodyLong>
            <Label as="p" spacing>
                Inntekt som ikke gir deg utbetaling av uføretrygd det året: 500 000 kr : {maksInntektsgrense}
            </Label>
            <BodyLong spacing>
                Hvis du tjener over denne summen får du ikke uføretrygd det aktuelle året. Neste vil du få uføretrygd igjen, dersom du ikke tjener for mye også det året.
            </BodyLong>
            <Label as="p" spacing>
                Barnetillegg har egne grenser
            </Label>
            <BodyLong spacing>
                Hvis du tjener over denne summen får du ikke uføretrygd det aktuelle året. Neste vil du få uføretrygd igjen, dersom du ikke tjener for mye også det året.
            </BodyLong>
        </ExpansionCard.Content>
    </ExpansionCard>
</>);

export const gradertUforetrygCard = (intektsgrense: string, kompensasjonsgrad: string, maksInntektsgrense: string) => (<>
    <ExpansionCard aria-label="Demo med bare tittel">
        <ExpansionCard.Header>
            <ExpansionCard.Title>Din inntektsgrense med mer</ExpansionCard.Title>
        </ExpansionCard.Header>
        <ExpansionCard.Content>
            <BodyLong spacing>
                Dette er grensene som avgjør hvor mye inntekt du kan ha før utbetalingen av uføretrygden din blir justert ned. Det vil likevel lønne seg å jobbe, fordi uføretrygd og inntekt er høyere enn uføretrygd alene.
            </BodyLong>
            <Label as="p" spacing>
                Din inntektsgrense: {intektsgrense}
            </Label>
            <BodyLong spacing>
                Hvis du får høyere inntekt enn inntektsgrensen, begynner vi å trekke en prosent av uføretrygd tilsvarende det beløpet du tjener over inntektsgrensen. Beløpet opp til inntektsgrensen blir du aldri trukket for.
            </BodyLong>
            <Label as="p" spacing>
                Din kompensasjonsgrad: {kompensasjonsgrad}
            </Label>
            <BodyLong spacing>
                Det betyr at hvis du tjener 1000 kr over inntektsgrensen, trekker vi 70% av 1000 kr fra uføretrygden din, det vil si at vi trekker 700 kr. Du vil fortsatt få 300 kr uføretrygd i tillegg til lønnen din.
            </BodyLong>
            <Label as="p" spacing>
                Inntekt som ikke gir deg utbetaling av uføretrygd det året: 500 000 kr : {maksInntektsgrense}
            </Label>
            <BodyLong spacing>
                Hvis du tjener over denne summen får du ikke uføretrygd det aktuelle året. Neste vil du få uføretrygd igjen, dersom du ikke tjener for mye også det året.
            </BodyLong>
            <Label as="p" spacing>
                Barnetillegg har egne grenser
            </Label>
            <BodyLong spacing>
                Hvis du tjener over denne summen får du ikke uføretrygd det aktuelle året. Neste vil du få uføretrygd igjen, dersom du ikke tjener for mye også det året.
            </BodyLong>
        </ExpansionCard.Content>
    </ExpansionCard>
</>);

export const varigTilrettlagtArbeid = (intektsgrense: string, kompensasjonsgrad: string, maksInntektsgrense: string) => (<>
        <ExpansionCard aria-label="Demo med bare tittel">
            <ExpansionCard.Header>
                <ExpansionCard.Title>Din inntektsgrense med mer</ExpansionCard.Title>
            </ExpansionCard.Header>
            <ExpansionCard.Content>
                <BodyLong spacing>
                    Dette er grensene som avgjør hvor mye inntekt du kan ha før utbetalingen av uføretrygden din blir justert ned. Det vil likevel lønne seg å jobbe, fordi uføretrygd og inntekt er høyere enn uføretrygd alene.
                </BodyLong>
                <Label as="p" spacing>
                    Din inntektsgrense: {intektsgrense}
                </Label>
                <BodyLong spacing>
                    Hvis du får høyere inntekt enn inntektsgrensen, begynner vi å trekke en prosent av uføretrygd tilsvarende det beløpet du tjener over inntektsgrensen. Beløpet opp til inntektsgrensen blir du aldri trukket for.
                </BodyLong>
                <Label as="p" spacing>
                    Din kompensasjonsgrad: {kompensasjonsgrad}
                </Label>
                <BodyLong spacing>
                    Det betyr at hvis du tjener 1000 kr over inntektsgrensen, trekker vi 70% av 1000 kr fra uføretrygden din, det vil si at vi trekker 700 kr. Du vil fortsatt få 300 kr uføretrygd i tillegg til lønnen din.
                </BodyLong>
                <Label as="p" spacing>
                    Inntekt som ikke gir deg utbetaling av uføretrygd det året: {maksInntektsgrense}
                </Label>
                <BodyLong spacing>
                    Hvis du tjener over denne summen får du ikke uføretrygd det aktuelle året. Neste vil du få uføretrygd igjen, dersom du ikke tjener for mye også det året.
                </BodyLong>
            </ExpansionCard.Content>
        </ExpansionCard>
</>);

