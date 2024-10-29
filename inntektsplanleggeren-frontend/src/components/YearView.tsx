import {Alert, ExpansionCard, Heading, Radio, RadioGroup, ReadMore, VStack} from "@navikt/ds-react";
import React, {useContext} from "react";
import {FormStateContext} from "@/context/FormData";

interface Props {
    availableYears: number[],
    infoType: number //0 default, 1 for oct/nov, 2 for dec
}

export function YearView({ availableYears, infoType }: Props) {
    const {selectedYear, setSelectedYear} = useContext(FormStateContext)

    const [firstYear, secondYear] = availableYears;

    // useEffect(() => {
    //     if (firstYear !== undefined && secondYear === undefined) {
    //         setSelectedYear(firstYear.toString(10));
    //     }
    // }, [firstYear, secondYear, setSelectedYear]);

    if (availableYears.length === 0) {
        return null;
    }

    if (availableYears.length === 1) {
        return (
            <>
                <Heading size={"medium"} level={"2"} spacing>Du kan registrere inntekter for {firstYear}</Heading>

                <Card />
            </>
        );
    }

    return (
        <>
            <Heading size="medium" level="2" spacing>Du kan registrere inntekter for {firstYear} og {secondYear}</Heading>

            <VStack gap="4">
                <Card />

                {infoType === 0 ? null : <Alert variant="info">{infoMessage(infoType)}</Alert>}

                <RadioGroup legend="Hvilket år ønsker du å registrere inntekter for?" value={selectedYear} onChange={setSelectedYear}>
                    {availableYears.map(year => <Radio key={year} value={year.toString(10)}>{year}</Radio>)}
                </RadioGroup>
            </VStack>
        </>
    )
}

const Card = () => (
    <ReadMore header="Å legge inn inntekt for andre år">
        Tekst Tekst Tekst Tekst Tekst
    </ReadMore>
);

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