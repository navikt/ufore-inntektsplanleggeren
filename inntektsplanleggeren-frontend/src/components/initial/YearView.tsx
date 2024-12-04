import { Heading, List, Radio, RadioGroup, ReadMore, VStack} from "@navikt/ds-react";
import React, {useContext, useEffect} from "react";
import {FormStateContext} from "@/context/FormData";

interface Props {
    availableYears: number[],
    error: string | null;
}

export function YearView({ availableYears, error }: Props) {
    const {selectedYear, setSelectedYear} = useContext(FormStateContext)

    const [firstYear, secondYear] = availableYears;

    useEffect(() => {
        if (firstYear !== undefined && secondYear === undefined) {
            setSelectedYear(firstYear);
        }
    }, [firstYear, secondYear, setSelectedYear]);

    if (availableYears.length === 0) {
        return null;
    }

    return (
        <VStack>
            { availableYears.length === 1 ?
                <Heading size={"medium"} level={"2"} spacing>Du kan registrere inntekter for {firstYear}</Heading> :
                <Heading size="medium" level="2">Du kan registrere inntekter for {firstYear} og {secondYear}</Heading>
            }

            <VStack gap="7">
                <ReadMore header="Tidspunkt for å registrere inntekt">
                        <List>
                            <List.Item>I perioden 1. januar - 30. september kan du bare legge inn inntekter for dette året.</List.Item>
                            <List.Item>Fra 1. oktober - 30. november kan du både legge inn inntekter for dette året og neste år.</List.Item>
                            <List.Item>Fra 1. til 31. desember kan du bare registrere inntekter for neste år, fordi endringen ikke vil påvirke utbetalingen din før til neste år.</List.Item>
                        </List>
                </ReadMore>

                { firstYear !== undefined && secondYear !== undefined ?
                    <RadioGroup error={error} legend="Hvilket år ønsker du å registrere inntekter for?" value={selectedYear} onChange={setSelectedYear}>
                        {availableYears.map(year => <Radio key={year} value={year}>{year.toString(10)}</Radio>)}
                    </RadioGroup> : null
                }
            </VStack>
        </VStack>
    )
}



