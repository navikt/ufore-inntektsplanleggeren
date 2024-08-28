import {Alert, ExpansionCard, Heading, Radio, RadioGroup} from "@navikt/ds-react";


export function YearView(props: {
    availableYears: number[],
    infoType: number
}) {
    const handleChange = (val: number) => console.info(val);

    return (
        <div>
            { props.availableYears.length > 1 ?
                <>
                    <Heading size={"medium"} level={"2"}>Du kan registrere inntekter for {props.availableYears[0]} og {props.availableYears[1]}</Heading>

                    <ExpansionCard aria-label="Demo med bare tittel" size="small">
                        <ExpansionCard.Header>
                            <ExpansionCard.Title>Å legge inn inntekt for andre år</ExpansionCard.Title>
                        </ExpansionCard.Header>
                        <ExpansionCard.Content>
                        Tekst Tekst Tekst Tekst Tekst
                        </ExpansionCard.Content>
                    </ExpansionCard>

                    {props.infoType !== 0 ? <>
                        <Alert variant="info">{infoMessage(props.infoType)}</Alert>
                        </> : <></>
                    }

                    <RadioGroup legend="Hvilket år ønsker du å registrere inntekter for?" onChange={handleChange}>
                        <Radio value={props.availableYears[0]}>{props.availableYears[0]}</Radio>
                        <Radio value={props.availableYears[1]}>{props.availableYears[1]}</Radio>
                    </RadioGroup>
                </>
                :
                <></>
            }
        </div>
    )
}

function infoMessage(infoType: number): string {
    switch (infoType) {
        case 1:
            return "Hvis du ikke sender inn ny forventet inntekt for neste år, vil vi mot slutten av året registrere at du har en tilsvarende inntekt som i år.";
        case 2:
            return "I desember kan du se hvilke inntekter som er registrert for året vi er inne i, men bare legge inn nye inntekter for neste år. Dette er fordi endring i utbetaling av uføretrygd skjer fra måneden etter at du har meldt inn ny inntekt.";
        default:
            return "";
    }
}