import {Alert, ExpansionCard, Heading, Radio, RadioGroup} from "@navikt/ds-react";
import {useContext} from "react";
import {DataContext} from "@/DataContextProvider";


export function YearView(props: {
    availableYears: number[],
    setYear: (value: number) => void
    infoType: number //0 default, 1 for oct/nov, 2 for dec
}) {
    // const handleChange = (val: number) => console.info(val);


    return (
        <div>
            { props.availableYears.length > 1 ?
                <>
                    <Heading size={"medium"} level={"2"}>Du kan registrere inntekter for {props.availableYears[0]} og {props.availableYears[1]}</Heading>
                    {card()}
                    {props.infoType !== 0 ? <>
                        <Alert variant="info">{infoMessage(props.infoType)}</Alert>
                        </> : <></>
                    }
                    <RadioGroup legend="Hvilket år ønsker du å registrere inntekter for?" onChange={props.setYear}>
                        <Radio value={props.availableYears[0]}>{props.availableYears[0]}</Radio>
                        <Radio value={props.availableYears[1]}>{props.availableYears[1]}</Radio>
                    </RadioGroup>
                </> :
                <>
                    <Heading size={"medium"} level={"2"}>Du kan registrere inntekter for {props.availableYears[0]}</Heading>
                    {props.setYear(props.availableYears[0])}
                    {card()}
                </>
            }
        </div>
    )
}

function card() {
    return (
    <>
        <ExpansionCard aria-label="Demo med bare tittel" size="small">
            <ExpansionCard.Header>
                <ExpansionCard.Title>Å legge inn inntekt for andre år</ExpansionCard.Title>
            </ExpansionCard.Header>
            <ExpansionCard.Content>
                Tekst Tekst Tekst Tekst Tekst
            </ExpansionCard.Content>
        </ExpansionCard>
    </>)
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